/*
 * Copyright (c) 2026, OpenSavvy and contributors.
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

package opensavvy.dokka.material.mkdocs.aggregator

import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

/**
 * Creates the 'reference' top-level page, which links to all modules.
 */
class ReferencePageTransformer : PageTransformer {
	override fun invoke(input: RootPageNode): RootPageNode {
		return input.transformContentPagesTree { page ->
			val content = page.content

			if (content !is ContentGroup)
				return@transformContentPagesTree page

			var found = false
			val newContent = content.copy(
				children = content.children.map { node ->
					when (node) {
						is ContentHeader if node.level == 2 && (node.children.firstOrNull() as? ContentText)?.text?.contains("modules", ignoreCase = true) == true -> {
							found = true
							ContentGroup(
								children = listOf(
									node.copy(
										level = 1,
										children = listOf(ContentText("Reference", node.dci, node.sourceSets))
									),
									ContentHeader(
										children = node.children.map { child ->
											if (child is ContentText)
												child.copy(text = child.text.replace(":", ""))
											else
												child
										},
										level = 2,
										dci = node.dci,
										sourceSets = node.sourceSets,
										style = node.style,
										extra = node.extra,
									)
								),
								dci = node.dci,
								sourceSets = node.sourceSets,
								style = node.style,
								extra = node.extra,
							)
						}

						is ContentTable if node.dci.kind == ContentKind.Main -> {
							// The multi-module page uses a table of kind Main to list modules.
							// We want to transform each row into a H3 header and a body.
							ContentGroup(
								children = node.children.flatMap { row ->
									val (header, body) = row.children
									listOfNotNull(
										(header as? ContentDRILink ?: header.children.filterIsInstance<ContentDRILink>().firstOrNull())?.let { link ->
											ContentHeader(
												children = listOf(link),
												level = 3,
												dci = link.dci,
												sourceSets = link.sourceSets,
												style = emptySet(),
											)
										} ?: (header as? ContentGroup)?.children?.filterIsInstance<ContentText>()?.firstOrNull { it.text.isNotBlank() }?.let { text ->
											ContentHeader(
												children = listOf(text),
												level = 3,
												dci = text.dci,
												sourceSets = text.sourceSets,
												style = emptySet(),
											)
										} ?: ContentHeader(
											children = listOf(header),
											level = 3,
											dci = header.dci,
											sourceSets = header.sourceSets,
											style = emptySet(),
										),
										body.takeIf { (it as? ContentGroup)?.children?.isNotEmpty() == true || (it !is ContentGroup) }
									)
								} + listOf(
									ContentBreakLine(node.sourceSets, node.dci),
									ContentText("<hr/>\n\nThis website is built with ", node.dci, node.sourceSets),
									ContentResolvedLink(
										children = listOf(ContentText("Dokka", node.dci, node.sourceSets)),
										address = "https://github.com/kotlin/dokka",
										extra = PropertyContainer.empty(),
										dci = node.dci,
										sourceSets = node.sourceSets
									),
									ContentText(" and ", node.dci, node.sourceSets),
									ContentResolvedLink(
										children = listOf(ContentText("Dokka for MkDocs", node.dci, node.sourceSets)),
										address = "https://dokka-mkdocs.opensavvy.dev/",
										extra = PropertyContainer.empty(),
										dci = node.dci,
										sourceSets = node.sourceSets
									),
									ContentText(". If you see any formatting issues, ", node.dci, node.sourceSets),
									ContentResolvedLink(
										children = listOf(ContentText("please report them", node.dci, node.sourceSets)),
										address = "https://gitlab.com/opensavvy/automation/dokka-material-mkdocs/-/issues/new",
										extra = PropertyContainer.empty(),
										dci = node.dci,
										sourceSets = node.sourceSets
									),
									ContentText(".", node.dci, node.sourceSets),
								),
								dci = node.dci,
								sourceSets = node.sourceSets,
								style = node.style,
								extra = node.extra
							)
						}

						else -> node
					}
				}
			)

			if (found) {
				page.modified(content = newContent)
			} else {
				page
			}
		}
	}
}
