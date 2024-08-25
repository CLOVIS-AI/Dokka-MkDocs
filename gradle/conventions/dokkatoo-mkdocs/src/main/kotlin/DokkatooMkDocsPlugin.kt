package opensavvy.dokka.gradle

import dev.adamko.dokkatoo.formats.DokkatooFormatPlugin
import dev.adamko.dokkatoo.internal.DokkatooInternalApi
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import java.io.File

@OptIn(DokkatooInternalApi::class)
abstract class DokkatooMkDocsPlugin : DokkatooFormatPlugin(formatName = "mkdocs") {

	override fun apply(target: Project) {
		super.apply(target)

		val dokkatooMkdocsModuleOutputDirectoriesResolver by target.configurations.getting

		val dokkatooCopyIntoMkDocs by target.tasks.registering(Sync::class) {
			group = "dokkatoo"
			description = "Copies the Dokkatoo pages into the website."

			from(dokkatooMkdocsModuleOutputDirectoriesResolver)
			into(target.layout.dir(target.provider { File("docs/api") }))

			eachFile {
				path = path.removePrefix("module/")
			}

			includeEmptyDirs = true
			duplicatesStrategy = DuplicatesStrategy.WARN //TODO make each module generate files in its own directory, afterwards remove this
		}

		val embedDokkaIntoMkDocs by target.tasks.registering {
			group = "dokkatoo"
			description = "Lifecycle task to embed configured Dokkatoo modules into a Material for MkDocs website"

			dependsOn(dokkatooCopyIntoMkDocs)
		}
	}

	override fun DokkatooFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:1.0.0")
		}
	}
}
