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

package opensavvy.dokka.material.mkdocs.renderer

import opensavvy.dokka.material.mkdocs.renderer3.MultilineSignatureStyle
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

private const val MIN_PARAMS_FOR_MULTILINE = 3
private const val MAX_LINE_LENGTH = 80

class SignatureBreaklineTransformer : PageTransformer {
    override fun invoke(input: RootPageNode): RootPageNode =
        input.transformContentPagesTree { page ->
            page.modified(
                content = page.content.recursiveMapTransform<ContentGroup, ContentNode> {
                    if (it.dci.kind == SymbolContentKind.Parameters && shouldWrapParams(it)) {
                        it.copy(
                            style = it.style + MultilineSignatureStyle.Wrapped,
                            children = it.children.map { child ->
                                (child as? ContentGroup)
                                    ?.copy(style = child.style + MultilineSignatureStyle.Indented)
                                    ?: child
                            }
                        )
                    } else {
                        it
                    }
                }
            )
        }

    private fun shouldWrapParams(group: ContentGroup): Boolean {
        val paramCount = group.children.count {
            (it as? ContentGroup)?.dci?.kind == SymbolContentKind.Parameter
        }
        return paramCount >= MIN_PARAMS_FOR_MULTILINE ||
               group.plainTextLength() > MAX_LINE_LENGTH
    }

    private fun ContentNode.plainTextLength(): Int = when (this) {
        is ContentText -> text.length
        is ContentGroup -> children.sumOf { it.plainTextLength() }
        else -> 0
    }
}
