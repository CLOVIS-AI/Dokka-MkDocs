package opensavvy.dokka.material.mkdocs
 
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

class MethodGroupingTransformer : PageTransformer {
	override fun invoke(input: RootPageNode): RootPageNode {
		val driToPage = mutableMapOf<DRI, MemberPageNode>()
		val allMergedPages = mutableSetOf<MemberPageNode>()

		// 1. Collect all member pages
		input.transformContentPagesTree { page ->
			if (page is MemberPageNode) {
				page.documentables.forEach { doc ->
					driToPage[doc.dri] = page
				}
			}
			page
		}

		return input.transformContentPagesTree { page ->
			if (page is ClasslikePageNode) {
				val classDRI = page.dri.firstOrNull() ?: return@transformContentPagesTree page
				val alreadyMerged = mutableSetOf<MemberPageNode>()
				// 2. Replace summary rows with full member content
				val newContent = page.content.recursiveMapTransform<ContentTable, ContentNode> { table ->
					if (table.dci.kind in listOf(ContentKind.Functions, ContentKind.Properties, ContentKind.Constructors)) {
						table.copy(
							children = table.children.map { row ->
								if (row is ContentGroup) {
									val dri = row.findDRI()
									val memberPage = dri?.let { driToPage[it] }
									if (memberPage != null && dri.packageName == classDRI.packageName) {
										if (memberPage !in alreadyMerged) {
											alreadyMerged.add(memberPage)
											allMergedPages.add(memberPage)
											row.copy(
												children = memberPage.content.children.map { it.demoteHeaders(2) }
											)
										} else {
											// Already merged as part of an overload group. Remove this row.
											ContentGroup(emptyList(), row.dci, row.sourceSets, row.style, row.extra)
										}
									} else {
										row
									}
								} else {
									row
								}
							}
						)
					} else {
						table
					}
				}

				// 3. Remove member pages from this class's children
				page.modified(
					content = newContent as ContentGroup,
					children = page.children.filterNot { it is MemberPageNode }
				)
			} else {
				page
			}
		}.transformContentPagesTree { page ->
			if (page is PackagePageNode) {
				val newContent = page.content.recursiveMapTransform<ContentTable, ContentNode> { table ->
					if (table.dci.kind in listOf(ContentKind.Functions, ContentKind.Properties)) {
						table.copy(
							children = table.children.filter { row ->
								val dri = row.findDRI()
								val memberPage = dri?.let { driToPage[it] }
								memberPage == null || memberPage !in allMergedPages
							}
						)
					} else {
						table
					}
				}
				page.modified(
					content = newContent as ContentGroup,
					children = page.children.filterNot { it in allMergedPages }
				)
			} else {
				page
			}
		}
	}

	private fun ContentNode.findDRI(): DRI? {
		if (this is ContentDRILink) return dci.dri.firstOrNull()
		if (this is ContentComposite) {
			for (child in this.children) {
				val dri = child.findDRI()
				if (dri != null) return dri
			}
		}
		return dci.dri.firstOrNull()
	}

	private fun ContentNode.demoteHeaders(by: Int): ContentNode {
		return this.recursiveMapTransform<ContentHeader, ContentNode> { header ->
			header.copy(level = header.level + by)
		}
	}
}
