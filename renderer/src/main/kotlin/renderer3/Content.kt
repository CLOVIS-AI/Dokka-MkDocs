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

internal fun RenderingContext.buildContent(node: ContentNode) {
	when (node) {
		is ContentText -> buildText(node)
		is ContentCodeInline -> buildCodeInline(node)
		is ContentHeader -> buildHeader(node)
		is PlatformHintedContent -> buildPlatformHinted(node)
		is ContentDivergentGroup -> buildPlatformDivergent(node)
		is ContentDRILink -> buildDRILink(node)
		is ContentResolvedLink -> buildResolvedLink(node)
		is ContentGroup -> buildGroup(node)
		is ContentTable -> buildTable(node)
		else -> appendParagraph("[Unknown content of type ${node::class}]")
	}
}
