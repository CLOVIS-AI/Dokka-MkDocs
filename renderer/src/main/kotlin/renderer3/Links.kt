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
import org.jetbrains.dokka.pages.ContentResolvedLink

internal fun RenderingContext.buildLink(address: String, isCode: Boolean = false, label: RenderingContext.() -> Unit) {
	if (isInCodeBlock) {
		append("<a href=\"$address\">")
		label()
		append("</a>")
	} else {
		append('[')
		if (isCode)
			append('`')
		label()
		if (isCode)
			append('`')
		append("]($address)")
	}
}

internal fun RenderingContext.buildDRILink(link: ContentDRILink) {
	val address = locations.resolve(link.address, link.sourceSets, page)

	if (address != null) {
		val linkAddress = if (isInCodeBlock && address.endsWith(".md")) {
			// In code blocks, links are emitted as raw <a> tags, so MkDocs can't resolve .md targets; convert them to .html.
			"${address.removeSuffix(".md")}.html"
		} else {
			address
		}

		buildLink(linkAddress, isCode = true) {
			buildGroup(link)
		}
	} else {
		buildGroup(link)
	}
}

internal fun RenderingContext.buildResolvedLink(link: ContentResolvedLink) {
	buildLink(link.address) {
		buildGroup(link)
	}
}
