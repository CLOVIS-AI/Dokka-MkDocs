package opensavvy.dokka.material.mkdocs

import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.transformers.pages.PageTransformer

@Suppress("UNCHECKED_CAST")
class OverloadGroupingTransformer : PageTransformer {
	override fun invoke(input: RootPageNode): RootPageNode {
		return input.transformContentPagesTree { page ->
			page.modified(content = groupOverloads(page.content))
		}
	}

	private fun groupOverloads(node: ContentNode): ContentNode {
		return when (node) {
			is ContentDivergentGroup -> groupOverloadsInDivergentGroup(node)
			is ContentGroup -> {
				val newChildren = node.children.map { groupOverloads(it) }
				val collapsedChildren = collapseConsecutiveOverloads(newChildren)
				node.copy(children = collapsedChildren)
			}
			else -> node
		}
	}

	private fun groupOverloadsInDivergentGroup(group: ContentDivergentGroup): ContentDivergentGroup {
		val instances = group.children.filterIsInstance<ContentComposite>()
		if (instances.size <= 1) return group

		val newInstances = mutableListOf<ContentNode>()
		var i = 0
		while (i < instances.size) {
			val current = instances[i]
			val currentChildren = current.children
			if (currentChildren.size >= 2) {
				val groupSignatures = mutableListOf(currentChildren[0])
				val groupDoc = currentChildren[1]
				val others = if (currentChildren.size > 2) currentChildren.drop(2) else emptyList()

				var j = i + 1
				while (j < instances.size &&
					instances[j].children.size == currentChildren.size &&
					isEquivalent(groupDoc, instances[j].children[1]) &&
					(others.isEmpty() || others.zip(instances[j].children.drop(2)).all { (a, b) -> isEquivalent(a, b) })
				) {
					groupSignatures.add(instances[j].children[0])
					j++
				}

				if (groupSignatures.size > 1) {
					val combinedSignatures = ContentGroup(groupSignatures, current.dci, current.sourceSets, current.style, current.extra)
					newInstances.add(
						ContentDivergentInstance(
							combinedSignatures,
							groupDoc,
							others.firstOrNull(),
							current.dci,
							current.sourceSets,
							current.style,
							current.extra
						)
					)
					i = j
				} else {
					newInstances.add(current)
					i++
				}
			} else {
				newInstances.add(current)
				i++
			}
		}
		return group.copy(children = newInstances as List<ContentDivergentInstance>)
	}

	private fun collapseConsecutiveOverloads(children: List<ContentNode>): List<ContentNode> {
		if (children.size <= 2) return children
		val result = mutableListOf<ContentNode>()
		var i = 0
		while (i < children.size) {
			val current = children[i]
			if (isSignature(current) && i + 1 < children.size && isDocumentation(children[i + 1])) {
				val groupSignatures = mutableListOf(current)
				val groupDoc = children[i + 1]
				i += 2
				while (i + 1 < children.size && isSignature(children[i]) && isEquivalent(groupDoc, children[i + 1])) {
					groupSignatures.add(children[i])
					i += 2
				}
				result.addAll(groupSignatures)
				result.add(groupDoc)
			} else {
				result.add(current)
				i++
			}
		}
		return result
	}

	private fun isSignature(node: ContentNode): Boolean =
		node is ContentGroup && node.style.contains(TextStyle.Monospace)

	private fun isDocumentation(node: ContentNode): Boolean =
		node is ContentGroup && !node.style.contains(TextStyle.Monospace)

	private fun isEquivalent(a: ContentNode, b: ContentNode): Boolean {
		if (a === b) return true
		if (a::class != b::class) return false

		return when (a) {
			is ContentText -> a.text == (b as ContentText).text
			is ContentCodeInline -> isEquivalentComposite(a, b as ContentCodeInline)
			is ContentCodeBlock -> a.language == (b as ContentCodeBlock).language && isEquivalentComposite(a, b)
			is ContentGroup -> a.style == (b as ContentGroup).style && isEquivalentComposite(a, b)
			is ContentTable -> isEquivalentTable(a, b as ContentTable)
			is ContentList -> isEquivalentComposite(a, b as ContentList)
			is ContentHeader -> a.level == (b as ContentHeader).level && isEquivalentComposite(a, b)
			is ContentDRILink -> isEquivalentComposite(a, b as ContentDRILink)
			is ContentResolvedLink -> a.address == (b as ContentResolvedLink).address && isEquivalentComposite(a, b)
			is ContentBreakLine -> true
			else -> a == b
		}
	}

	private fun isEquivalentComposite(a: ContentComposite, b: ContentComposite): Boolean {
		if (a.children.size != b.children.size) return false
		return a.children.zip(b.children).all { (c1, c2) -> isEquivalent(c1, c2) }
	}

	private fun isEquivalentTable(a: ContentTable, b: ContentTable): Boolean {
		if (a.header.size != b.header.size) return false
		if (!a.header.zip(b.header).all { (c1, c2) -> isEquivalent(c1, c2) }) return false
		if (a.children.size != b.children.size) return false
		return a.children.zip(b.children).all { (c1, c2) -> isEquivalent(c1, c2) }
	}
}
