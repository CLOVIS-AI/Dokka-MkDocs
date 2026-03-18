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

import org.jetbrains.dokka.base.templating.toJsonString
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.ContentDRILink

private const val deferredLinkCommandDelimiter = "\u1680"

/**
 * The current module doesn't know how to resolve this link.
 *
 * For example, because this is a link to a class defined in another module.
 *
 * The current module serializes a special marker in the documentation,
 * and the aggregator plugin will re-attempt to resolve it later.
 */
class DeferredLinkCommand(
	val dri: DRI,
	/**
	 * The value of [RenderingContext.isInCodeBlock] at the place where the link is inserted.
	 */
	val isInCodeBlock: Boolean,
	/**
	 * Whether the link should be displayed as Markdown inline code.
	 */
	val isCode: Boolean,
) {

	fun format(address: String, renderedLabel: String): String = buildString {
		buildLink(
			address = address,
			isInCodeBlock = isInCodeBlock,
			isCode = isCode,
		) {
			append(renderedLabel)
		}
	}

	companion object {
		val deferredCommandRegex: Regex =
			Regex("<!---$deferredLinkCommandDelimiter GfmCommand ([^$deferredLinkCommandDelimiter ]*)$deferredLinkCommandDelimiter--->(.+?)(?=<!---$deferredLinkCommandDelimiter)<!---$deferredLinkCommandDelimiter--->")

		val MatchResult.deferredLinkCommand: String
			get() = groupValues[1]

		val MatchResult.deferredLinkLabel: String
			get() = groupValues[2]
	}
}

internal fun RenderingContext.buildDeferredLink(
	link: ContentDRILink,
	isCode: Boolean,
	content: RenderingContext.() -> Unit,
) {
	val command = DeferredLinkCommand(
		dri = link.address,
		isInCodeBlock = this.isInCodeBlock,
		isCode = isCode,
	)

	append("<!---$deferredLinkCommandDelimiter GfmCommand ${toJsonString(command)}$deferredLinkCommandDelimiter--->")
	content()
	append("<!---$deferredLinkCommandDelimiter--->")
}
