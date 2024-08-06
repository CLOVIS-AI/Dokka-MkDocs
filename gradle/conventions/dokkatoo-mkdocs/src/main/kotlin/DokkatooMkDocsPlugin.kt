package opensavvy.dokka.gradle

import dev.adamko.dokkatoo.formats.DokkatooFormatPlugin
import dev.adamko.dokkatoo.internal.DokkatooInternalApi
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named

@OptIn(DokkatooInternalApi::class)
abstract class DokkatooMkDocsPlugin : DokkatooFormatPlugin(formatName = "mkdocs") {

	override fun apply(target: Project) {
		super.apply(target)

		val materialForMkDocsPages by target.configurations.creating {
			isCanBeConsumed = true
			isCanBeResolved = true

			attributes {
				attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category.DOCUMENTATION))
				attribute(Bundling.BUNDLING_ATTRIBUTE, target.objects.named(Bundling.EXTERNAL))
				attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, target.objects.named("mkdocs-pages"))
			}
		}

		target.artifacts.add(materialForMkDocsPages.name, target.tasks.named("dokkatooGeneratePublicationMkdocs"))
	}

	override fun DokkatooFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:1.0.0")
		}
	}
}
