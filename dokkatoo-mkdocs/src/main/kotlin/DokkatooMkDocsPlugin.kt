package opensavvy.dokka.gradle

import dev.adamko.dokkatoo.formats.DokkatooFormatPlugin
import dev.adamko.dokkatoo.internal.DokkatooInternalApi
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

@OptIn(DokkatooInternalApi::class)
abstract class DokkatooMkDocsPlugin : DokkatooFormatPlugin(formatName = "mkdocs") {

	override fun apply(target: Project) {
		super.apply(target)

		val dokkatooMkdocsModuleOutputDirectoriesResolver by target.configurations.getting

		val siteOutput = target.layout.projectDirectory.dir("docs/api")
		val navOutput = target.layout.buildDirectory.file("mkdocs/navigation.yaml")

		val dokkatooCopyIntoMkDocs by target.tasks.registering(Sync::class) {
			group = "dokkatoo"
			description = "Copies the Dokkatoo pages into the website."

			from(dokkatooMkdocsModuleOutputDirectoriesResolver)
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

			inputs.files(dokkatooCopyIntoMkDocs)
			outputs.file(navOutput)

			doLast {
				val root = siteOutput.asFile
				val builder = StringBuilder()
				var depth = 1

				root.walkTopDown()
					.onEnter { file ->
						// Ignore directories that only contain other directories
						file.walkBottomUp().any { it.isFile }
					}
					.onLeave {
						if (it.isDirectory)
							depth--
					}
					.forEach { file ->
						val relative = file.relativeTo(root)

						val indent = "    ".repeat(depth) + "  "

						when {
							file == root -> {
								builder.appendLine("  - Reference (experimental):")
							}

							file.isDirectory -> {
								builder.appendLine("$indent- ${relative.name.decodeAsDokkaUrl()}:")
								depth++
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

			dependsOn(dokkatooCopyIntoMkDocs, embedMkDocsNavigation)
		}
	}

	override fun DokkatooFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:$DokkaMkDocsVersion")
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
