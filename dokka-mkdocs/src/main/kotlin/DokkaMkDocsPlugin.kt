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
				val builder = StringBuilder()

				root.walkBottomUp()
					.onEnter { file ->
						// Ignore directories that only contain other directories
						file.walkBottomUp().any { it.isFile }
					}
					.sortedWith { a, b ->
						// Very ugly code :/ Our goal:
						// ①. A directory should be immediately before its contents
						// ②. If a file has the same name as a directory, it should be just after the directory's contents
						// ③. Everything else should be placed alphabetically.

						val aR = a.relativeTo(root)
						val bR = b.relativeTo(root)

						when {
							a.isDirectory && b in a -> -1
							b.isDirectory && a in b -> 1
							else -> {
								// \u00001 because it's the smallest invisible character, so directories will be listed first
								val aPath = aR.path.split(File.separatorChar).joinToString("\u0001") { it.decodeAsDokkaUrl() }
								val bPath = bR.path.split(File.separatorChar).joinToString("\u0001") { it.decodeAsDokkaUrl() }
								aPath.compareTo(bPath)
							}
						}
					}
					.forEach { file ->
						val relative = file.relativeTo(root)
						val depth = relative.path.count { it == File.separatorChar } + 1
						val indent = "    ".repeat(depth) + "  "

						when {
							file == root -> {
								builder.appendLine("  - Reference (experimental):")
							}

							file.isDirectory -> {
								builder.appendLine("$indent- \"${relative.name.decodeAsDokkaUrl()}\":")
							}

							file.isFile && file.name.endsWith(".md") -> {
								builder.appendLine("$indent- api/$relative")
							}
						}
					}

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

fun String.decodeAsDokkaUrl(): String {
	if (this.contains('-') && !this.startsWith("-")) {
		return this.split('-').joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
	}
	var result = this
	var index: Int
	while (result.indexOf('-').also { index = it } >= 0) {
		result = result.substring(0 until index) + result[index + 1].uppercase() + result.substring(index + 2)
	}
	return result
}

operator fun File.contains(child: File): Boolean {
	val childPath = child.path.split(File.separatorChar)
	val parentPath = this.path.split(File.separatorChar)

	for ((i, it) in parentPath.withIndex()) {
		if (childPath.getOrNull(i) != it) return false
	}

	return true
}
