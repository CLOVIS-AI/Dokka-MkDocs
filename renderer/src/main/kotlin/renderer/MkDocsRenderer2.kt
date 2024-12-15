package opensavvy.dokka.material.mkdocs.renderer

import opensavvy.dokka.material.mkdocs.GfmCommand.Companion.templateCommand
import opensavvy.dokka.material.mkdocs.MaterialForMkDocsPlugin
import opensavvy.dokka.material.mkdocs.ResolveLinkGfmCommand
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.base.renderers.DefaultRenderer
import org.jetbrains.dokka.links.PointingToDeclaration
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

	override fun StringBuilder.wrapGroup(node: ContentGroup, pageContext: ContentPage, childrenCallback: StringBuilder.() -> Unit) {
		val styles = decorations.fromGroupStyles(node.style)

		if (node.dci.kind == ContentKind.Main && node.children.firstOrNull()?.dci?.kind == ContentKind.Symbol) {
			appendLine("***")
		}

		styles.iterator().wrapIn(this) {
			buildComment { "CONTENT GROUP $node" }

			if (ContentKind.shouldBePlatformTagged(node.dci.kind)) {
				buildPlatformMarkers(node.sourceSets)
			}

			childrenCallback()
		}
	}

	override fun StringBuilder.buildCodeBlock(code: ContentCodeBlock, pageContext: ContentPage) {
		append("\n```")
		append(code.language.ifEmpty { "kotlin" })
		appendLine()
		code.children.forEach {
			if (it is ContentText) {
				// since this is a code block where text will be rendered as is,
				// no need to escape text, apply styles, etc. Just need the plain value
				append(it.text)
			} else if (it is ContentBreakLine) {
				// since this is a code block where text will be rendered as is,
				// there's no need to add tailing slash for line breaks
				appendLine()
			}
		}
		appendLine()
		append("```")
		appendLine()
	}

	override fun StringBuilder.buildCodeInline(code: ContentCodeInline, pageContext: ContentPage) {
		append("`")
		code.children.filterIsInstance<ContentText>().forEach { append(it.text) }
		append("`")
	}

	override fun StringBuilder.buildText(textNode: ContentText) {
		val styles = decorations.fromInlineStyles(textNode.style)

		styles.iterator().wrapIn(this) {
			buildComment { "TEXT $textNode" }
			append(textNode.text)
		}
	}

	override fun StringBuilder.buildTable(node: ContentTable, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
		buildComment { "TABLE NODE $node" }

		when (node.dci.kind) {
			ContentKind.Packages, ContentKind.Classlikes, ContentKind.Functions, ContentKind.Constructors, ContentKind.Properties -> {
				for (pkg in node.children) {
					for (child in pkg.children) {
						when (child) {
							is ContentDRILink -> {
								buildHeader(3, ContentHeader(listOf(child), 3, child.dci, child.sourceSets, child.style, child.extra)) {
									buildDRILink(child, pageContext, sourceSetRestriction)
								}
							}
							else -> {
								buildContentNode(child, pageContext, sourceSetRestriction)
							}
						}
					}
				}
			}
		}
	}

	override fun StringBuilder.buildResource(node: ContentEmbeddedResource, pageContext: ContentPage) {
		buildComment { "RESOURCE NODE $node" }
	}

	override fun StringBuilder.buildNavigation(page: PageNode) {
		var first = true

		locationProvider.ancestors(page).asReversed()
			.filter { it.name.isNotBlank() }
			.forEach { node ->
				if (first) {
					first = false
				} else {
					append(" • ")
				}

				if (node.isNavigable) buildLink(node, page)
				else append(node.name)
			}

		appendLine()
		appendLine()
	}

	override fun StringBuilder.buildList(node: ContentList, pageContext: ContentPage, sourceSetRestriction: Set<DisplaySourceSet>?) {
		buildComment { "LIST NODE $node" }
	}

	// region Links

	private fun StringBuilder.buildLink(to: PageNode, from: PageNode) =
		buildLink(locationProvider.resolve(to, from, skipExtension = true)!! + ".html") {
			append(to.name)
		}

	override fun StringBuilder.buildLink(address: String, content: StringBuilder.() -> Unit) {
		append("<a href=\"$address\">")
		content()
		append("</a>")
	}

	override fun StringBuilder.buildDRILink(
		node: ContentDRILink,
		pageContext: ContentPage,
		sourceSetRestriction: Set<DisplaySourceSet>?,
	) {
		var location = locationProvider.resolve(node.address, node.sourceSets, pageContext)
			?.removeSuffix(".md")
			?.plus(".html")
		if (location == null) {
			val isPartial = context.configuration.delayTemplateSubstitution

			if (isPartial) {
				templateCommand(ResolveLinkGfmCommand(node.address)) {
					buildText(node.children, pageContext, sourceSetRestriction)
				}
			} else {
				buildText(node.children, pageContext, sourceSetRestriction)
			}
		} else {
			if (node.address.packageName != null && node.address.classNames == null && node.address.callable == null && node.address.target == PointingToDeclaration) {
				// We're reading a package definition.
				// For some reason, Dokka generates a full path for them,

				location = location.replaceBefore("/", "").removePrefix("/")
			}

			buildLink(location) {
				buildText(node.children, pageContext, sourceSetRestriction)
			}
		}
	}

	// endregion

	override fun StringBuilder.buildLineBreak() {
		appendLine("<br/>")
	}

	override fun StringBuilder.buildHeader(level: Int, node: ContentHeader, content: StringBuilder.() -> Unit) {
		val decorator = when (level) {
			1 -> Decoration.ofPrefix("# ")
			2 -> Decoration.ofPrefix("## ")
			3 -> Decoration.ofPrefix("### ")
			4 -> Decoration.ofPrefix("#### ")
			5 -> Decoration.ofPrefix("##### ")
			else -> Decoration.ofPrefix("###### ")
		}

		decorator.wrapIn(this) {
			buildComment { "HEADER $node" }
			content()
		}

		appendLine()
	}

	// region Overall page rendering

	/**
	 * Overall layout of each page.
	 */
	override fun buildPageContent(context: StringBuilder, page: ContentPage) = with(context) {
		// Front matter
		appendLine("---")
		appendLine("tags:")
		(page as? WithDocumentables)
			?.documentables
			?.flatMapTo(HashSet()) { it.sourceSets }
			?.map { it.analysisPlatform.key }
			?.forEach {
				appendLine(" - $it")
			}
		page.content
			.children
			.flatMap {
				if (it is ContentGroup)
					it.children
				else listOf(it)
			}
			.filterIsInstance<ContentHeader>()
			.firstOrNull { it.level == 1 }
			?.children
			?.filterIsInstance<ContentText>()
			?.joinToString(" ") { it.text }
			?.also { appendLine("title: $it") }
		appendLine("---")

		// Navigation
		context.buildNavigation(page)

		// Contents
		page.content.build(context, page)
	}

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

	private val INSERT_COMMENTS = true

	private inline fun StringBuilder.buildComment(content: () -> String) {
		if (INSERT_COMMENTS) {
			append("<!-- ")
			append(content())
			append(" -->")
		}
	}

	fun StringBuilder.buildPlatformMarkers(sourceSets: Iterable<DisplaySourceSet>) {
		for (sourceSet in sourceSets) {
			append("<span class=\"md-typeset md-tag\">${sourceSet.name}</span>")
		}
	}
}

private val PageNode.isNavigable: Boolean
	get() = this !is RendererSpecificPage || strategy != RenderingStrategy.DoNothing
