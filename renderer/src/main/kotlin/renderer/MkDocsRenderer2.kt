package opensavvy.dokka.material.mkdocs.renderer

import opensavvy.dokka.material.mkdocs.MaterialForMkDocsPlugin
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.base.renderers.DefaultRenderer
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query
import org.jetbrains.dokka.transformers.pages.PageTransformer

open class MkDocsRenderer2(
	context: DokkaContext,
) : DefaultRenderer<StringBuilder>(context) {
	override val preprocessors: List<PageTransformer> = context.plugin<MaterialForMkDocsPlugin>().query { mkdocsPreprocessor }

	override fun buildError(node: ContentNode) {
		context.logger.warn("MkDocs renderer has encountered problem. The unmatched node is $node")
	}

	override fun StringBuilder.buildText(textNode: ContentText) {
		appendLine("TEXT NODE $textNode\n")
	}

	override fun StringBuilder.buildTable(node: ContentTable, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
		appendLine("TABLE NODE $node\n")
	}

	override fun StringBuilder.buildResource(node: ContentEmbeddedResource, pageContext: ContentPage) {
		appendLine("RESOURCE NODE $node\n")
	}

	override fun StringBuilder.buildNavigation(page: PageNode) {
		appendLine("NAVIGATION NODE $page\n")
	}

	override fun StringBuilder.buildList(node: ContentList, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
		appendLine("LIST NODES $node\n")
	}

	override fun StringBuilder.buildLink(address: String, content: StringBuilder.() -> Unit) {
		appendLine("LINK NODE $address\n")
	}

	override fun StringBuilder.buildLineBreak() {
		appendLine("<br/>")
	}

	override fun StringBuilder.buildHeader(level: Int, node: ContentHeader, content: StringBuilder.() -> Unit) {
		appendLine("HEADER $node\n")
	}

	// region Overall page rendering

	override suspend fun renderPage(page: PageNode) {
		val path by lazy {
			locationProvider.resolve(page, skipExtension = true)
				?: throw DokkaException("Cannot resolve path for ${page.name}")
		}

		return when (page) {
			is ContentPage -> outputWriter.write(path, buildPage(page) { c, p -> buildPageContent(c, p) }, ".md")
			is RendererSpecificPage -> when (val strategy = page.strategy) {
				is RenderingStrategy.Copy -> outputWriter.writeResources(strategy.from, path)
				is RenderingStrategy.Write -> outputWriter.write(path, strategy.text, "")
				is RenderingStrategy.Callback -> outputWriter.write(path, strategy.instructions(this, page), ".md")
				is RenderingStrategy.DriLocationResolvableWrite -> outputWriter.write(
					path,
					strategy.contentToResolve { dri, sourcesets ->
						locationProvider.resolve(dri, sourcesets)
					},
					""
				)

				is RenderingStrategy.PageLocationResolvableWrite -> outputWriter.write(
					path,
					strategy.contentToResolve { pageToLocate, context ->
						locationProvider.resolve(pageToLocate, context)
					},
					""
				)

				RenderingStrategy.DoNothing -> Unit
			}

			else -> throw AssertionError(
				"Page ${page.name} cannot be rendered by renderer as it is not renderer specific nor contains content"
			)
		}
	}

	override fun buildPage(page: ContentPage, content: (StringBuilder, ContentPage) -> Unit): String =
		buildString {
			content(this, page)
		}.trim().replace("\n[\n]+".toRegex(), "\n\n")

	// endregion
}
