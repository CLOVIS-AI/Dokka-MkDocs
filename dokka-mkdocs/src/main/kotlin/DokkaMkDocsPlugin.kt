package opensavvy.dokka.gradle

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.dokka.gradle.formats.DokkaFormatPlugin
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateModuleTask
import java.io.File

abstract class DokkaMkDocsPlugin : DokkaFormatPlugin(formatName = "mkdocs") {

	private lateinit var moduleOutputFiles: Provider<List<File>>

	@OptIn(InternalDokkaGradlePluginApi::class)
	override fun DokkaFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:$DokkaMkDocsVersion")
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
		target.dependencies.add("dokkaMkdocsPublicationPlugin", "org.jetbrains.dokka:all-modules-page-plugin:$DokkaVersion")
		target.dependencies.add("dokkaMkdocsPublicationPlugin", "dev.opensavvy.dokka.mkdocs:aggregator:$DokkaMkDocsVersion")

		// Use the Gradle project's leaf name (e.g. "example-core") as the module path instead of
		// the full project path (e.g. "example/example-core"). This gives clean output URLs.
		target.tasks.withType(DokkaGenerateModuleTask::class.java).configureEach {
			modulePath.set(target.name)
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
			duplicatesStrategy = DuplicatesStrategy.FAIL
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
