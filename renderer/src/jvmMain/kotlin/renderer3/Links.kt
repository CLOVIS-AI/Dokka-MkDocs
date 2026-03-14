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

internal fun Appendable.buildLink(
	address: String,
	isInCodeBlock: Boolean,
	isCode: Boolean,
	label: Appendable.() -> Unit,
) {
	if (isInCodeBlock) {
		// mkdocs cannot replace links within code blocks, so we do the .md -> .html substitution directly.
		val actualAddress = if (address.endsWith(".md"))
			address.removeSuffix(".md") + ".html"
		else
			address

		append("<a href=\"$actualAddress\">")
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
	val resolvedAddress = locations.resolve(link.address, link.sourceSets, page)

	// Links to the module-level page shouldn't appear as code, since the module has a display name
	// Apparently, Dokka represents this as the package name '.ext'.
	val isCode = link.address.packageName != ".ext"

	if (resolvedAddress != null) {
		buildLink(
			resolveInternalLink(resolvedAddress),
			isInCodeBlock = this.isInCodeBlock,
			isCode = isCode,
		) {
			buildGroup(link)
		}
	} else {
		buildDeferredLink(
			link = link,
			isCode = isCode,
		) {
			buildGroup(link)
		}
	}
}

internal fun RenderingContext.buildResolvedLink(link: ContentResolvedLink) {
	buildLink(
		link.address,
		isInCodeBlock = this.isInCodeBlock,
		isCode = false,
	) {
		buildGroup(link)
	}
}

private fun RenderingContext.resolveInternalLink(address: String): String {
	return if (isInCodeBlock && address.endsWith(".md")) {
		// In code blocks, links are emitted as raw <a> tags, so MkDocs can't resolve .md targets; convert them to .html.
		"${address.removeSuffix(".md")}.html"
	} else {
		address
	}
}
