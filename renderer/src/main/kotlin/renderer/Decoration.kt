package opensavvy.dokka.material.mkdocs.renderer

import org.jetbrains.dokka.pages.Style
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.pages.TokenStyle

// region Data structure

fun interface Decoration {

	fun StringBuilder.wrap(content: StringBuilder.() -> Unit)

	companion object {
		fun ofSpan(className: String) =
			Decoration { content ->
				append("<span class=\"$className\">")
				content()
				append("</span>")
			}

		fun ofElement(elementName: String) =
			Decoration { content ->
				append("<$elementName>")
				content()
				append("</$elementName>")
			}

		fun ofPrefix(prefix: String) =
			Decoration { content ->
				append(prefix)
				content()
			}

		val NoOp = Decoration {content ->
			content()
		}
	}
}

// endregion
// region DSL declaration

class Decorations {

	private val groups = ArrayList<Pair<Style, Decoration>>()
	private val inlines = ArrayList<Pair<Style, Decoration>>()

	constructor(block: Decorations.() -> Unit) {
		block()
	}

	fun group(style: Style, block: Decoration) {
		groups += style to block
	}

	fun inline(style: Style, block: Decoration) {
		inlines += style to block
	}

	fun fromGroupStyles(style: Iterable<Style>) = style
		.mapNotNull { groups.find { (style, _) -> style == it } }
		.map { it.second }

	fun fromInlineStyles(style: Iterable<Style>) = style
		.mapNotNull { inlines.find { (style, _) -> style == it } }
		.map { it.second }
}

// endregion
// region Generate from Dokka data

val decorations = Decorations {
	inline(TextStyle.Block, Decoration.ofElement("strong"))
	inline(TextStyle.Italic, Decoration.ofElement("em"))

	group(TextStyle.Paragraph, Decoration.ofElement("p"))
	group(TextStyle.Monospace) { content ->
		append("<div class=\"highlight\">")
		append("<pre>")
		append("<code class=\"md-code__content\">")
		append("<span>")
		content()
		append("</span>")
		append("</code>")
		append("</pre>")
		append("</div>")
	}

	inline(TokenStyle.Keyword, Decoration.ofSpan("kd"))
	inline(TokenStyle.Punctuation, Decoration.ofSpan("p"))
	inline(TokenStyle.Function, Decoration.ofSpan("nf"))
	inline(TokenStyle.Operator, Decoration.ofSpan("o"))
	inline(TokenStyle.Annotation, Decoration.ofSpan("se"))
	inline(TokenStyle.Number, Decoration.ofSpan("mi"))
	inline(TokenStyle.String, Decoration.ofSpan("s"))
	inline(TokenStyle.Boolean, Decoration.ofSpan("kc"))
	inline(TokenStyle.Constant, Decoration.ofSpan("nb"))
}

// endregion
// region Helpers

fun Decoration.wrapIn(builder: StringBuilder, block: StringBuilder.() -> Unit) = with(builder) {
	wrap(block)
}

fun Iterator<Decoration>.wrapIn(builder: StringBuilder, block: StringBuilder.() -> Unit) {
	when {
		!hasNext() -> block(builder)
		else -> next().wrapIn(builder) { wrapIn(builder, block) }
	}
}

// endregion
