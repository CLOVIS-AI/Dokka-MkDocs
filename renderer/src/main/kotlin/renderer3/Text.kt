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

private val textStyles: Map<Style, Decorator> = mapOf(
	TokenStyle.Keyword to Decorator.ofSpan("kd"),
	TokenStyle.Punctuation to Decorator.ofSpan("p"),
	TokenStyle.Function to Decorator.ofSpan("nf"),
	TokenStyle.Operator to Decorator.ofSpan("o"),
	TokenStyle.Annotation to Decorator.ofSpan("se"),
	TokenStyle.Number to Decorator.ofSpan("mi"),
	TokenStyle.String to Decorator.ofSpan("s"),
	TokenStyle.Boolean to Decorator.ofSpan("kc"),
	TokenStyle.Constant to Decorator.ofSpan("nb"),

	TextStyle.Strong to Decorator.ofElement("strong"),
	TextStyle.Italic to Decorator.ofElement("em"),
	TextStyle.Strikethrough to Decorator.ofElement("s"),
)

internal fun RenderingContext.buildText(node: ContentText) {
	decorateWith(textStyles.matches(node)) {
		append(node.text)
	}

	if (node.hasStyle(TokenStyle.Annotation) && node.text != "@") {
		appendLine()
	}
}

internal fun RenderingContext.buildCodeInline(node: ContentCodeInline) {
	if (node.language.isNotBlank()) {
		append("`#!${node.language} ")
	} else {
		append("`")
	}
	buildGroup(node)
	append("`")
}

internal fun RenderingContext.buildCodeBlock(node: ContentCodeBlock) {
	appendLine("```${node.language}")
	buildGroup(node)
	appendLine("\n```")
	appendLine()
}
