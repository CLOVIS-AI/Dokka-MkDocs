package opensavvy.dokka.material.mkdocs
 
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

class MethodGroupingTransformer : PageTransformer {
	override fun invoke(input: RootPageNode): RootPageNode {
		val driToPage = mutableMapOf<DRI, MemberPageNode>()
		val allMergedPages = mutableSetOf<MemberPageNode>()

		// 1. Collect all member pages and all potential target pages (types)
		val typeDRIs = mutableSetOf<DRI>()
		input.transformContentPagesTree { page ->
			if (page is MemberPageNode) {
				page.documentables.forEach { doc ->
					driToPage[doc.dri] = page
				}
			}
			if (page is ClasslikePageNode) {
				page.dri.forEach { typeDRIs.add(it) }
			}
			page
		}

		val fakeConstructorsByClass = mutableMapOf<DRI, MutableList<MemberPageNode>>()
		driToPage.forEach { (dri, page) ->
			// A fake constructor is a top-level function (classNames == null) 
			// with the same name as a type in the same package, 
			// AND it returns that type.
			if (dri.classNames == null && dri.callable != null) {
				val targetTypeDRI = typeDRIs.firstOrNull { 
					it.packageName == dri.packageName && it.classNames == dri.callable!!.name 
				}
				if (targetTypeDRI != null) {
					val returnsTargetType = page.documentables.any { doc ->
						doc.dri == dri && doc is DFunction && doc.type.extractDRI() == targetTypeDRI
					}
					if (returnsTargetType) {
						fakeConstructorsByClass.getOrPut(targetTypeDRI) { mutableListOf() }.add(page)
					}
				}
			}
		}

		return input.transformContentPagesTree { page ->
			if (page is ClasslikePageNode) {
				val classDRI = page.dri.firstOrNull() ?: return@transformContentPagesTree page
				val fakeConstructors = page.dri.flatMap { fakeConstructorsByClass[it] ?: emptyList() }.distinct()
				
				// Important: mark them as merged so they are removed from the package page
				allMergedPages.addAll(fakeConstructors)

				val pageContent = if (fakeConstructors.isNotEmpty()) {
					injectFakeConstructors(page.content as ContentGroup, fakeConstructors)
				} else {
					page.content
				}

				val alreadyMerged = mutableSetOf<MemberPageNode>()
				// 2. Replace summary rows with full member content
				val newContent = pageContent.recursiveMapTransform<ContentTable, ContentNode> { table ->
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
												children = memberPage.content.children.mapIndexed { index, child ->
													// index == 0: first block in the page, description of the class
													// index > 0: later blocks, method/property/constructor details
													child.demoteHeaders(if (index == 0) 2 else 3)
												}
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

	private fun injectFakeConstructors(content: ContentGroup, fakeConstructors: List<MemberPageNode>): ContentGroup {
		// Try to find the Main group where tables usually live
		val mainGroupIndex = content.children.indexOfFirst { it is ContentGroup && it.dci.kind == ContentKind.Main }
		if (mainGroupIndex != -1) {
			val mainGroup = content.children[mainGroupIndex] as ContentGroup
			val newMainGroup = injectFakeConstructors(mainGroup, fakeConstructors)
			val newChildren = content.children.toMutableList()
			newChildren[mainGroupIndex] = newMainGroup
			return content.copy(children = newChildren)
		}

		val existingTableIndex = content.children.indexOfFirst { it is ContentTable && it.dci.kind == ContentKind.Constructors }
		if (existingTableIndex != -1) {
			val table = content.children[existingTableIndex] as ContentTable
			val newRows = fakeConstructors.map { createRowFor(it) }
			val newChildren = content.children.toMutableList()
			newChildren[existingTableIndex] = table.copy(children = table.children + newRows)
			return content.copy(children = newChildren)
		} else {
			val newRows = fakeConstructors.map { createRowFor(it) }
			val dci = DCI(content.dci.dri, ContentKind.Constructors)
			val header = ContentHeader(
				children = listOf(ContentText("Constructors", dci, content.sourceSets)),
				level = 2,
				dci = dci,
				sourceSets = content.sourceSets,
				style = emptySet()
			)
			val table = ContentTable(
				header = emptyList(),
				children = newRows,
				dci = dci,
				sourceSets = content.sourceSets,
				style = emptySet()
			)

			// Insert before first existing table or level 2 header, or at the end
			val insertIndex = content.children.indexOfFirst {
				it is ContentTable || (it is ContentHeader && it.level == 2)
			}.let { if (it == -1) content.children.size else it }

			val newChildren = content.children.toMutableList()
			newChildren.add(insertIndex, header)
			newChildren.add(insertIndex + 1, table)
			return content.copy(children = newChildren)
		}
	}

	private fun createRowFor(memberPage: MemberPageNode): ContentGroup {
		val documentable = memberPage.documentables.first()
		val dri = documentable.dri
		val sourceSets = memberPage.content.sourceSets
		val dci = DCI(setOf(dri), ContentKind.Main)
		return ContentGroup(
			children = listOf(ContentDRILink(
				children = listOf(ContentText(memberPage.name, dci, sourceSets)),
				address = dri,
				dci = dci,
				sourceSets = sourceSets
			)),
			dci = dci,
			sourceSets = sourceSets,
			style = emptySet()
		)
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

	private fun Bound.extractDRI(): DRI? = when (this) {
		is TypeConstructor -> this.dri
		else -> null
	}
}
