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
								builder.appendLine("  - Reference:")
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

		val embedDokkaIntoMkDocs by target.tasks.registering {
			group = "dokkatoo"
			description = "Lifecycle task to embed configured Dokkatoo modules into a Material for MkDocs website"

			dependsOn(dokkatooCopyIntoMkDocs, generateMkDocsNavigation)
		}
	}

	override fun DokkatooFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:1.0.0")
		}
	}
}

fun String.decodeAsDokkaUrl(): String {
	return if (this[0] == '-') {
		this[1].uppercase() + this.substring(2)
	} else {
		this
	}
}
