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
import org.jetbrains.dokka.pages.PageNode

internal fun RenderingContext.buildLink(to: PageNode, from: PageNode) =
	buildLink(locations.resolve(to, from, skipExtension = true)!! + ".html") {
		append(to.name)
	}

internal fun RenderingContext.buildLink(address: String, label: RenderingContext.() -> Unit) {
	append("<a href=\"$address\">")
	label()
	append("</a>")
}

internal fun RenderingContext.buildDRILink(link: ContentDRILink) {
	var address = locations.resolve(link.address, link.sourceSets, page)

	if (address != null && address.endsWith(".md")) {
		address = address.removeSuffix(".md") + ".html"
	}

	if (address != null) {
		buildLink(address) {
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
