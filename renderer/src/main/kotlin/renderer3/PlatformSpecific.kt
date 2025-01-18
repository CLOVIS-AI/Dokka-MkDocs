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

import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentDivergentGroup
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.PlatformHintedContent

private fun RenderingContext.platformMarker(sourceSet: DisplaySourceSet) {
	append("<span class=\"md-typeset md-tag\">${sourceSet.platform.name}</span>")
}

private fun RenderingContext.platformMarkers(sourceSets: Iterable<DisplaySourceSet>) {
	append("<span class=\"md-tags\">")
	for (sourceSet in sourceSets) {
		platformMarker(sourceSet)
	}
	append("</span>")
}

internal fun RenderingContext.buildPlatformHinted(node: PlatformHintedContent) {
	buildPlatformsGroup(node.inner)
}

internal fun RenderingContext.buildPlatformDivergent(node: ContentDivergentGroup) {
	for (instance in node.children) {
		for (group in instance.children) {
			buildPlatformsGroup(group)
		}
	}
}

private fun RenderingContext.buildPlatformsGroup(group: ContentNode) {
	append("\n<div markdown>\n\n")
	val bySourceSet = group.children.groupBy { child ->
		child.sourceSets.joinToString(", ") { it.name }
	}

	for ((sourceSet, specific) in bySourceSet) {
		appendLine("=== \"${sourceSet}\"")
		appendLine()

		val childWriter = StringBuilder()
		val childContext = this.copy(writer = childWriter)
		for (child in specific) {
			childContext.buildContent(child)
			appendLine()
			appendLine()
		}
		childWriter.split("\n")
			.map { "    $it" }
			.forEach { appendLine(it) }

		appendLine()
	}
	appendLine("\n</div>\n")
}
