/*
 * Copyright 2014-2024 JetBrains s.r.o & OpenSavvy. Use of this source code is governed by the Apache 2.0 license.
 */

package opensavvy.dokka.material.mkdocs.location

import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.plugability.DokkaContext
import java.util.*

class MarkdownLocationProvider(
	pageGraphRoot: RootPageNode,
	dokkaContext: DokkaContext,
) : DokkaLocationProvider(pageGraphRoot, dokkaContext, ".md") {

	/**
	 * Overrides path building to skip the intermediate module-name directory for children of
	 * [ModulePageNode]. In the default Dokka behavior, module children are placed under
	 * `<module-display-name>/`, which creates an undesirable extra directory level in MkDocs output.
	 */
	override val pathsIndex: Map<PageNode, List<String>> = run {
		val index = IdentityHashMap<PageNode, List<String>>()

		fun PageNode.mkdocsPathName(): String =
			if (this is PackagePageNode || this is RendererSpecificResourcePage) name
			else identifierToFilename(name)

		fun registerPath(page: PageNode, prefix: List<String>) {
			when (page) {
				is RootPageNode if page.forceTopLevelName -> {
					index[page] = prefix + PAGE_WITH_CHILDREN_SUFFIX
					page.children.forEach { registerPath(it, prefix) }
				}

				is ModulePageNode -> {
					index[page] = prefix
					// Skip adding the module display name as a path segment for its children
					page.children.forEach { registerPath(it, prefix) }
				}

				else -> {
					val newPrefix = prefix + page.mkdocsPathName()
					index[page] = newPrefix
					page.children.forEach { registerPath(it, newPrefix) }
				}
			}
		}

		index[pageGraphRoot] = emptyList()
		pageGraphRoot.children.forEach { registerPath(it, emptyList()) }
		index
	}

	class Factory(private val context: DokkaContext) : LocationProviderFactory {
		override fun getLocationProvider(pageNode: RootPageNode): LocationProvider =
			MarkdownLocationProvider(pageNode, context)
	}
}
