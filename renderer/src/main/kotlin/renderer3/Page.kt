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

import org.jetbrains.dokka.pages.*

internal fun RenderingContext.buildFrontMatter() {
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
		?.also { appendLine("title: \"$it\"") }
	appendLine("---")
	appendLine()
}

internal fun RenderingContext.buildNavigation() {
	var first = true

	locations.ancestors(page).asReversed()
		.filter { it.name.isNotBlank() }
		.takeIf { it.size > 1 }.orEmpty() // Don't render the navigation bar for module pages
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

private val PageNode.isNavigable: Boolean
	get() = this !is RendererSpecificPage || strategy != RenderingStrategy.DoNothing
