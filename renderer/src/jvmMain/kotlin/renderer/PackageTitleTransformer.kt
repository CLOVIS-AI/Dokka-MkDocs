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

import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

/**
 * Renames the package's page title from 'Package-level declarations' to the package's technical name.
 */
class PackageTitleTransformer : PageTransformer {
	override fun invoke(input: RootPageNode): RootPageNode {
		return input.transformContentPagesTree { page ->
			if (page is PackagePageNode) {
				page.modified(
					content = page.content.recursiveMapTransform<ContentHeader, ContentNode> { header ->
						if (header.level == 1) {
							header.copy(
								children = listOf(
									ContentText(
										text = page.name,
										dci = header.dci,
										sourceSets = header.sourceSets,
										style = header.style,
										extra = header.extra
									)
								)
							)
						} else {
							header
						}
					}
				)
			} else {
				page
			}
		}
	}
}
