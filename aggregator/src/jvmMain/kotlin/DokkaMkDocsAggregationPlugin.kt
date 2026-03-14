/*
 * Copyright 2014-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

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

package opensavvy.dokka.material.mkdocs.aggregator

import opensavvy.dokka.material.mkdocs.MaterialForMkDocsPlugin
import opensavvy.dokka.material.mkdocs.location.MarkdownLocationProvider
import org.jetbrains.dokka.allModulesPage.AllModulesPagePlugin
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.Extension
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement
import org.jetbrains.dokka.templates.TemplateProcessingStrategy
import org.jetbrains.dokka.templates.TemplatingPlugin
import org.jetbrains.dokka.transformers.pages.PageTransformer

class DokkaMkDocsAggregationPlugin : DokkaPlugin() {

	private val allModulesPagePlugin by lazy { plugin<AllModulesPagePlugin>() }
	private val templateProcessingPlugin by lazy { plugin<TemplatingPlugin>() }
	private val mkdocsPlugin by lazy { plugin<MaterialForMkDocsPlugin>() }
	private val dokkaBase by lazy { plugin<DokkaBase>() }

	/**
	 * Processes GFM template commands (e.g., cross-module `ResolveLinkGfmCommand`) in generated `.md` files.
	 * Must be registered before the fallback strategy.
	 */
	val mkdocsTemplatingStrategy: Extension<TemplateProcessingStrategy, *, *> by extending {
		(templateProcessingPlugin.templateProcessingStrategy
			providing ::DokkaMkDocsTemplatingStrategy
			order { before(templateProcessingPlugin.fallbackProcessingStrategy) })
	}

	/**
	 * Overrides the location provider for the aggregated publication to use Markdown (`.md`) links
	 * instead of the default HTML links, and to resolve cross-module references correctly.
	 */
	val mkdocsLocationProvider: Extension<LocationProviderFactory, *, *> by extending {
		dokkaBase.locationProviderFactory providing MarkdownMultimoduleLocationProvider::Factory override listOf(
			mkdocsPlugin.locationProvider,
			allModulesPagePlugin.multimoduleLocationProvider,
		)
	}

	/**
	 * Overrides the partial location provider (used when resolving cross-module links from partial outputs)
	 * to use Markdown (`.md`) links.
	 */
	val mkdocsPartialLocationProvider: Extension<LocationProviderFactory, *, *> by extending {
		allModulesPagePlugin.partialLocationProviderFactory providing MarkdownLocationProvider::Factory override allModulesPagePlugin.baseLocationProviderFactory
	}

	val referencePageTransformer: Extension<PageTransformer, *, *> by extending {
		mkdocsPlugin.mkdocsPreprocessor providing { ReferencePageTransformer() } order {
			before(mkdocsPlugin.rootCreator)
		}
	}

	@OptIn(DokkaPluginApiPreview::class)
	override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement =
		PluginApiPreviewAcknowledgement
}
