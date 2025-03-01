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
import org.jetbrains.dokka.gradle.internal.DokkaInternalApi
import java.io.File

abstract class DokkaMkDocsPlugin : DokkaFormatPlugin(formatName = "mkdocs") {

	private lateinit var moduleOutputFiles: Provider<List<File>>

	@OptIn(DokkaInternalApi::class)
	override fun DokkaFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:$DokkaMkDocsVersion")
		}

		moduleOutputFiles = formatDependencies.moduleOutputDirectories.incomingArtifactFiles
	}

	override fun apply(target: Project) {
		super.apply(target)

		val siteOutput = target.layout.projectDirectory.dir("docs/api")
		val navOutput = target.layout.buildDirectory.file("mkdocs/navigation.yaml")

		val dokkaCopyIntoMkDocs by target.tasks.registering(Sync::class) {
			group = "dokkatoo"
			description = "Copies the Dokkatoo pages into the website."

			from(moduleOutputFiles)
			into(siteOutput)

			eachFile {
				path = path.removePrefix("module/")
			}

			exclude("includes/*")

			includeEmptyDirs = true
			duplicatesStrategy = DuplicatesStrategy.WARN // TODO make each module generate files in its own directory, afterwards remove this
		}

		val generateMkDocsNavigation by target.tasks.registering {
			group = "dokkatoo"
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
							aR.isDirectory && aR in bR -> -1
							bR.isDirectory && bR in aR -> 1
							else -> aR.path.replace(File.separatorChar, '\u0001').decodeAsDokkaUrl().compareTo(bR.path.replace(File.separatorChar, '\u0001').decodeAsDokkaUrl())
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
			group = "dokkatoo"
			description = "Adds all the generated files to the index of the MkDocs site"

			inputs.files(generateMkDocsNavigation)
			inputs.file(mkdocsYaml)
			outputs.file(mkdocsYaml)

			doLast {
				val startMarker = "# !!! EMBEDDED DOKKA START, DO NOT COMMIT !!! #"
				val endMarker = "# !!! EMBEDDED DOKKA END, DO NOT COMMIT !!! #"

				val lines = mkdocsYaml.asFile.readLines()
				val start = lines.takeWhile { it != startMarker }
				val end = lines.takeLastWhile { it != endMarker }

				val embeds = navOutput.get().asFile.readLines()

				val output = start + startMarker + embeds + endMarker + end
				mkdocsYaml.asFile.writeText(output.joinToString(System.lineSeparator()) + System.lineSeparator())
			}
		}

		val embedDokkaIntoMkDocs by target.tasks.registering {
			group = "dokkatoo"
			description = "Lifecycle task to embed configured Dokkatoo modules into a Material for MkDocs website"

			dependsOn(dokkaCopyIntoMkDocs, embedMkDocsNavigation)
		}
	}
}

fun String.decodeAsDokkaUrl(): String {
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
