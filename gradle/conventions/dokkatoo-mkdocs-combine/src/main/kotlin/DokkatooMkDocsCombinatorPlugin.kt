package opensavvy.dokka.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import java.io.File

class DokkatooMkDocsCombinatorPlugin : Plugin<Project> {

	override fun apply(target: Project) {
		val mkdocsWebsite by target.configurations.creating {
			isCanBeConsumed = false

			attributes {
				attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category.DOCUMENTATION))
				attribute(Bundling.BUNDLING_ATTRIBUTE, target.objects.named(Bundling.EXTERNAL))
				attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, target.objects.named("mkdocs-pages"))
			}
		}

		val embedDokkaIntoMkDocs by target.tasks.registering(Sync::class) {
			from(mkdocsWebsite)
			into(target.layout.dir(target.provider { File("docs/api") }))
		}
	}
}
