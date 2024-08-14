/*
 * Copyright 2014-2024 JetBrains s.r.o & OpenSavvy. Use of this source code is governed by the Apache 2.0 license.
 */

package opensavvy.dokka.material.mkdocs.location

import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext

class MarkdownLocationProvider(
	pageGraphRoot: RootPageNode,
	dokkaContext: DokkaContext,
) : DokkaLocationProvider(pageGraphRoot, dokkaContext, ".md") {

	init {
		println("Path index:")
		for ((page, path) in pathsIndex) {
			println(" • ${page.name} -> ${path.joinToString("/")}")
		}
	}

	override fun pathTo(node: PageNode, context: PageNode?): String {
		val default = super.pathTo(node, context)

		return when (node) {
			is ModulePageNode -> "${dokkaContext.configuration.moduleName}/$default"
			else -> {
				return default
				error(
					"""
						*** Found unexpected page type *** 
						Page graph root: ${pageGraphRoot.pretty()}
						Module name:     ${dokkaContext.configuration.moduleName}
						Node:            ${node.pretty()}
						Context:         ${context?.pretty()}
						Default path:    $default
					""".trimIndent()
				)
			}
		}.also {
			println()
			println(
				"""
					Generated a path:
					   Path for:         ${node.pretty()}
					   Context:          ${context?.pretty()}
					   Generated path:   $it
				""".trimIndent()
			)
		}
	}

	class Factory(private val context: DokkaContext) : LocationProviderFactory {
		override fun getLocationProvider(pageNode: RootPageNode): LocationProvider =
			MarkdownLocationProvider(pageNode, context)
	}
}

private fun PageNode.pretty() = "$name (${this::class})"
