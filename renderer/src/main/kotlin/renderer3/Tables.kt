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

import org.jetbrains.dokka.pages.ContentDRILink
import org.jetbrains.dokka.pages.ContentHeader
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentTable

internal fun RenderingContext.buildTable(node: ContentTable) {
	when (node.dci.kind) {
		ContentKind.Packages, ContentKind.Classlikes,
		ContentKind.Functions, ContentKind.Constructors,
		ContentKind.Properties,
			-> {
			buildTableAsSections(node)
		}

		ContentKind.Inheritors -> {
			buildTableAsList(node)
		}

		else -> appendParagraph("[Unknown table of kind ${node.dci.kind}]")
	}
}

private fun RenderingContext.buildTableAsSections(node: ContentTable) {
	for (pkg in node.children) {
		for (child in pkg.children) {
			if (child is ContentDRILink) {
				buildHeader(
					ContentHeader(
						listOf(child),
						3,
						child.dci,
						child.sourceSets,
						child.style,
						child.extra,
					)
				)
			} else {
				buildContent(child)
			}
		}
	}
}

private fun RenderingContext.buildTableAsList(node: ContentTable) {
	appendLine()
	appendLine()

	for (child in node.children) {
		append(" - ")
		buildContent(child)
		appendLine()
	}

	appendLine()
	appendLine()
}
