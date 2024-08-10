package opensavvy.dokka.gradle

import dev.adamko.dokkatoo.formats.DokkatooFormatPlugin
import dev.adamko.dokkatoo.internal.DokkatooInternalApi
import org.gradle.kotlin.dsl.dependencies

@OptIn(DokkatooInternalApi::class)
abstract class DokkatooMkDocsPlugin : DokkatooFormatPlugin(formatName = "mkdocs") {

	override fun DokkatooFormatPluginContext.configure() {
		project.dependencies {
			dokkaPlugin("dev.opensavvy.dokka.mkdocs:renderer:1.0.0")
		}
	}
}
