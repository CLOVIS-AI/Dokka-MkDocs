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

import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.Style
import org.jetbrains.dokka.pages.hasStyle

internal fun interface Decorator {
	fun RenderingContext.decorate(content: RenderingContext.() -> Unit)

	companion object {
		fun ofPrefix(prefix: String) = Decorator { content ->
			append(prefix)
			content()
		}

		fun ofElement(elementName: String) = Decorator { content ->
			append("<$elementName>")
			content()
			append("</$elementName>")
		}

		fun ofSpan(className: String) = Decorator { content ->
			append("<span class=\"$className\">")
			content()
			append("</span>")
		}
	}
}

internal fun RenderingContext.decorateWith(decorators: Sequence<Decorator>, block: RenderingContext.() -> Unit) {
	decorateWith(decorators.iterator(), block)
}

private fun RenderingContext.decorateWith(decorators: Iterator<Decorator>, block: RenderingContext.() -> Unit) {
	when {
		!decorators.hasNext() -> block(this)
		else -> with(decorators.next()) {
			decorate {
				decorateWith(decorators, block)
			}
		}
	}
}

internal fun Map<out Style, Decorator>.matches(node: ContentNode): Sequence<Decorator> = asSequence()
	.filter { (style, _) ->
		node.hasStyle(style)
	}
	.map { (_, decorator) -> decorator }
