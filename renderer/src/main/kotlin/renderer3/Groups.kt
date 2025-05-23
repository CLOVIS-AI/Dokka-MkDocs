/*
 * Copyright (c) 2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.dokka.material.mkdocs.renderer3

import org.jetbrains.dokka.pages.ContentComposite
import org.jetbrains.dokka.pages.TextStyle

private val groupStyles = mapOf(
	TextStyle.Monospace to Decorator { content ->
		if (!this.isInCodeBlock)
			append("<div class=\"highlight\"><pre><code class=\"md-code__content\"><span markdown>")
		with(this.copy(isInCodeBlock = true)) {
			content()
		}
		if (!this.isInCodeBlock)
			append("\n</span></code></pre></div>")
	},
	TextStyle.Paragraph to Decorator { content ->
		content()
		appendLine()
		appendLine()
	},
	TextStyle.Quotation to Decorator { content ->
		appendLine("<blockquote markdown>")
		appendLine()
		content()
		appendLine()
		appendLine("</blockquote>")
	}
)

internal fun RenderingContext.buildGroup(node: ContentComposite) {
	decorateWith(groupStyles.matches(node)) {
		for (child in node.children) {
			buildContent(child)
		}
	}
}
