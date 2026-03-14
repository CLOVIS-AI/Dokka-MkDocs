package opensavvy.dokka.gradle

import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.formats.DokkaFormatPlugin
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateModuleTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.File

abstract class DokkaMkDocsPlugin : DokkaFormatPlugin(formatName = "mkdocs") {

	private lateinit var moduleOutputFiles: Provider<List<File>>

	@OptIn(InternalDokkaGradlePluginApi::class)
	override fun DokkaFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:$DokkaMkDocsVersion")
		}

		// all-modules-page-plugin goes into dokkaMkdocsPublicationPlugin (added in apply() below).
		// It is an external Maven artifact with no Dokka-specific Gradle metadata, so Gradle
		// resolves it directly as a JAR without any attribute-matching issues.

		// The aggregator is a local project that also applies DokkaMkDocsPlugin. If it were added to
		// dokkaMkdocsPublicationPlugin (DokkaClasspathAttribute=dokka-publication-plugins), Gradle
		// would select the aggregator project's empty dokkaMkdocsPublicationPluginApiOnlyConsumable
		// instead of its JAR (because that consumable matches DokkaClasspathAttribute=dokka-publication-plugins).
		//
		// Fix: use a separate configuration with DokkaClasspathAttribute=dokka-plugins. That value has
		// no matching consumable on the aggregator project, so Gradle falls back to runtimeElements → JAR.
		val aggregatorBucket = project.configurations.create("dokkaMkdocsAggregatorPlugin~internal") {
			isCanBeResolved = false
			isCanBeConsumed = false
		}
		project.dependencies.add("dokkaMkdocsAggregatorPlugin~internal", "dev.opensavvy.dokka.mkdocs:aggregator:$DokkaMkDocsVersion")

		val aggregatorResolver = project.configurations.create("dokkaMkdocsAggregatorPluginResolver~internal") {
			isCanBeResolved = true
			isCanBeConsumed = false
			isTransitive = false
			extendsFrom(aggregatorBucket)
			attributes {
				attribute(Usage.USAGE_ATTRIBUTE, project.objects.named<Usage>(Usage.JAVA_RUNTIME))
				attribute(Attribute.of("org.jetbrains.dokka.format", String::class.java), formatName)
				attribute(Attribute.of("org.jetbrains.dokka.classpath", String::class.java), "dokka-plugins")
			}
		}

		dokkaTasks.generatePublication.configure {
			generator.pluginsClasspath.from(aggregatorResolver)
		}

		moduleOutputFiles = project.layout.buildDirectory.dir("dokka/mkdocs").map { listOf(it.asFile) }
	}

	@OptIn(InternalDokkaGradlePluginApi::class)
	override fun apply(target: Project) {
		super.apply(target)

		// Add all-modules-page-plugin to the publication-only classpath.
		// "dokkaMkdocsPublicationPlugin" is used only by dokkaGeneratePublicationMkdocs,
		// not by the per-module dokkaGenerateModuleMkdocs task.
		// This prevents all-modules-page-plugin from suppressing singleGeneration in module tasks.
		// (The aggregator is handled differently in configure() above.)
		target.dependencies.add("dokkaMkdocsPublicationPlugin", "org.jetbrains.dokka:all-modules-page-plugin:$DokkaVersion")

		// Use the Gradle project's leaf name (e.g. "example-core") as the module path instead of
		// the full project path (e.g. "example/example-core"). This gives clean output URLs.
		target.tasks.withType(DokkaGenerateModuleTask::class.java).configureEach {
			modulePath.set(target.name)
		}

		// Fix: DGP v2's KotlinAdapter excludes the metadata compilation when building classpath lists.
		// This leaves shared KMP source sets (e.g. commonMain) with only source directories on their
		// analysis classpath, causing "Unresolved reference" errors for stdlib symbols.
		// Detect these source sets and supply the JVM compilation classpath.
		target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
			fixKmpCommonSourceSetClasspath(target)
		}

		val siteOutput = target.layout.projectDirectory.dir("docs/api")
		val navOutput = target.layout.buildDirectory.file("mkdocs/navigation.yaml")

		val dokkaCopyIntoMkDocs by target.tasks.registering(Sync::class) {
			group = GROUP
			description = "Copies the Dokkatoo pages into the website."

			val dokkaTasks = target.tasks.matching { it.name.startsWith("dokkaGenerate") && it.name.endsWith("Mkdocs") }
			dependsOn(dokkaTasks)

			from(moduleOutputFiles)
			into(siteOutput)

			val gitignore = File(siteOutput.asFile, ".gitignore")
			doLast {
				gitignore.writeText("*")
			}

			includeEmptyDirs = true
			duplicatesStrategy = DuplicatesStrategy.WARN // TODO make each module generate files in its own directory, afterwards remove this
		}

		val generateMkDocsNavigation by target.tasks.registering {
			group = GROUP
			description = "Scans the generated documentation files to generate the MkDocs index"

			inputs.files(dokkaCopyIntoMkDocs)
			outputs.file(navOutput)

			doLast {
				val root = siteOutput.asFile
				val files = root.walkBottomUp()
					.onEnter { file ->
						// Ignore directories that only contain other directories
						file.walkBottomUp().any { it.isFile }
					}.toList()

				// 1. Determine logical parents
				val logicalParents = mutableMapOf<File, File>()
				val allDirs = files.filter { it.isDirectory }.sortedBy { it.path.length }

				for (dir in allDirs) {
					if (dir == root) continue
					val physicalParent = dir.parentFile

					// Look for sibling packages that are parents of this one
					val logicalParent = allDirs.filter {
						it.parentFile == physicalParent &&
							dir.name.startsWith(it.name + ".")
					}.maxByOrNull { it.name.length }

					logicalParents[dir] = logicalParent ?: physicalParent
				}

				for (file in files.filter { it.isFile }) {
					logicalParents[file] = file.parentFile
				}

				// 2. Build the tree
				class Node(val file: File, val name: String) {
					val children = mutableListOf<Node>()

					fun sort() {
						children.sortWith { a, b ->
							if (a.file.name == "index.md") return@sortWith -1
							if (b.file.name == "index.md") return@sortWith 1

							val aIsDir = a.file.isDirectory
							val bIsDir = b.file.isDirectory

							if (aIsDir && bIsDir) {
								val aIsPackage = a.name.startsWith("Package ")
								val bIsPackage = b.name.startsWith("Package ")
								if (aIsPackage != bIsPackage) return@sortWith if (aIsPackage) -1 else 1
							}

							if (aIsDir != bIsDir) return@sortWith if (aIsDir) -1 else 1

							a.name.compareTo(b.name, ignoreCase = true)
						}
						children.forEach { it.sort() }
					}

					fun render(builder: StringBuilder, depth: Int) {
						val indent = "    ".repeat(depth) + "  "
						val relative = file.relativeTo(root)

						when {
							file == root -> {
								builder.appendLine("  - Reference:")
								children.forEach { it.render(builder, 1) }
							}

							file.isDirectory -> {
								val displayName = if (name.startsWith("Package ")) {
									name.removePrefix("Package ")
								} else {
									name
								}
								builder.appendLine("$indent- \"$displayName\":")
								children.forEach { it.render(builder, depth + 1) }
							}

							file.isFile && file.name.endsWith(".md") -> {
								builder.appendLine("$indent- api/$relative")
							}
						}
					}
				}

				val nodes = mutableMapOf<File, Node>()
				val rootNode = Node(root, "")
				nodes[root] = rootNode

				for (file in files.sortedBy { it.path.length }) {
					if (file == root) continue
					val logicalParent = logicalParents[file] ?: root
					val parentNode = nodes[logicalParent] ?: rootNode
					val isParentPackage = parentNode.name.startsWith("Package ")
					val isParentModule = logicalParent.isDirectory && logicalParent.parentFile == root

					var displayName = file.name
					var isPackage = false
					if (file.isDirectory) {
						if (isParentModule) {
							isPackage = true
						} else if (isParentPackage) {
							if (file.name.startsWith(logicalParent.name + ".")) {
								displayName = file.name.removePrefix(logicalParent.name + ".")
								isPackage = true
							}
						}
					}

					val isModule = logicalParent == root && file.isDirectory
					val prettyName = if (isModule)
						displayName
					else
						displayName.decodeAsDokkaUrl(capitalize = !isPackage)

					val finalName = if (isPackage) "Package $prettyName" else prettyName

					val node = Node(file, finalName)
					nodes[file] = node
					parentNode.children.add(node)
				}

				rootNode.sort()
				val builder = StringBuilder()
				rootNode.render(builder, 0)

				navOutput.get().asFile.writeText(builder.toString())
			}
		}

		val mkdocsYaml = target.layout.projectDirectory.file("mkdocs.yml")
		val embedMkDocsNavigation by target.tasks.registering {
			group = GROUP
			description = "Adds all the generated files to the index of the MkDocs site"

			inputs.files(generateMkDocsNavigation)
			inputs.file(mkdocsYaml)
			outputs.file(mkdocsYaml)

			doLast {
				val lines = mkdocsYaml.asFile.readLines()
				val start = lines.takeWhile { it != startMarker }
				val end = lines.takeLastWhile { it != endMarker }

				val embeds = navOutput.get().asFile.readLines()

				val output = start + startMarker + embeds + endMarker + end
				mkdocsYaml.asFile.writeText(output.joinToString(System.lineSeparator()) + System.lineSeparator())
			}
		}

		val removeMkDocsNavigation by target.tasks.registering {
			group = GROUP
			description = "Removes all the generated files to the index of the MkDocs site"

			onlyIf("Does this project have a mkdocs.yml file?") { mkdocsYaml.asFile.exists() }

			inputs.file(mkdocsYaml)
			outputs.file(mkdocsYaml)

			doLast {
				val lines = mkdocsYaml.asFile.readLines()
				val start = lines.takeWhile { it != startMarker }
				val end = lines.takeLastWhile { it != endMarker }

				val output = start + startMarker + endMarker + end
				mkdocsYaml.asFile.writeText(output.joinToString(System.lineSeparator()) + System.lineSeparator())
			}
		}

		val embedDokkaIntoMkDocs by target.tasks.registering {
			group = GROUP
			description = "Lifecycle task to embed configured Dokkatoo modules into a Material for MkDocs website"

			dependsOn(dokkaCopyIntoMkDocs, embedMkDocsNavigation)
		}

		target.tasks.named("clean") {
			dependsOn("cleanDokkaCopyIntoMkDocs", removeMkDocsNavigation)
		}
	}

	@OptIn(InternalDokkaGradlePluginApi::class)
	private fun fixKmpCommonSourceSetClasspath(target: Project) {
		val kmpExtension = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
		val dokkaExtension = target.extensions.getByType(DokkaExtension::class.java)

		// configureEach fires lazily during task-graph resolution, after all build files have been
		// evaluated, so kmpExtension.targets and compilations are fully populated at that point.
		dokkaExtension.dokkaSourceSets.configureEach {
			val ksName = name

			// Non-metadata main compilations (e.g. jvmMain, jsMain)
			val nonMetadataMain = kmpExtension.targets
				.filter { it.platformType != KotlinPlatformType.common }
				.mapNotNull { it.compilations.findByName("main") }
			if (nonMetadataMain.isEmpty()) return@configureEach

			// Source sets directly owned by a compilation already have a classpath from DGP
			val ownedNames = nonMetadataMain
				.flatMapTo(mutableSetOf()) { it.kotlinSourceSets.map { s -> s.name } }
			if (ksName in ownedNames) return@configureEach

			// For a shared/common source set, find the compilations that include it transitively
			val kss = kmpExtension.sourceSets.findByName(ksName) ?: return@configureEach
			val compilation = nonMetadataMain
				.filter { kss in it.allKotlinSourceSets }
				.let { list ->
					// Prefer JVM — its stdlib JAR also contains common stdlib declarations
					list.firstOrNull { it.target.platformType == KotlinPlatformType.jvm }
						?: list.firstOrNull()
				}
				?: return@configureEach

			classpath.from(compilation.compileDependencyFiles)
		}
	}

	companion object {
		private const val startMarker = "# !!! EMBEDDED DOKKA START, DO NOT COMMIT !!! #"
		private const val endMarker = "# !!! EMBEDDED DOKKA END, DO NOT COMMIT !!! #"
		private const val GROUP = "Dokka MkDocs"
	}
}

fun String.decodeAsDokkaUrl(capitalize: Boolean = true): String {
	val decoded = if (this.contains('-') && !this.startsWith("-")) {
		this.split('-').joinToString(" ") {
			if (capitalize) it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
			else it
		}
	} else {
		var result = this
		var index: Int
		while (result.indexOf('-').also { index = it } >= 0) {
			val nextChar = result.getOrNull(index + 1)
			result = result.substring(0 until index) + ((if (capitalize) nextChar?.uppercase() else nextChar?.toString()) ?: "") + result.substring((index + 2).coerceAtMost(result.length))
		}
		result
	}
	return if (capitalize) decoded.replaceFirstChar { it.uppercaseChar() } else decoded
}

operator fun File.contains(child: File): Boolean {
	val childPath = child.path.split(File.separatorChar)
	val parentPath = this.path.split(File.separatorChar)

	for ((i, it) in parentPath.withIndex()) {
		if (childPath.getOrNull(i) != it) return false
	}

	return true
}
