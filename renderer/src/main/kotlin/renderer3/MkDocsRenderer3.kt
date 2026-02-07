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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import opensavvy.dokka.material.mkdocs.MaterialForMkDocsPlugin
import org.jetbrains.dokka.DokkaException
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.resolvers.local.LocationProvider
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.Renderer

class MkDocsRenderer3(
	private val context: DokkaContext,
) : Renderer {
	private val outputWriter = context.plugin<DokkaBase>().querySingle { outputWriter }
	private val preprocessors = context.plugin<MaterialForMkDocsPlugin>().query { mkdocsPreprocessor }
	private lateinit var locationProvider: LocationProvider

	override fun render(root: RootPageNode) {
		val newRoot = preprocessors.fold(root) { acc, t -> t(acc) }

		locationProvider =
			context.plugin<DokkaBase>().querySingle { locationProviderFactory }.getLocationProvider(newRoot)

		runBlocking(Dispatchers.Default) {
			renderPages(newRoot)
		}
	}

	private fun CoroutineScope.renderPages(page: PageNode) {
		launch {
			renderPage(page)
		}

		for (child in page.children)
			renderPages(child)
	}

	private suspend fun renderPage(page: PageNode) {
		val path by lazy {
			locationProvider.resolve(page, skipExtension = true)
				?: throw DokkaException("Cannot resolve path for ${page.name}")
		}
		when (page) {
			is ContentPage -> outputWriter.write(path, buildPage(page), ".md")
			is RendererSpecificPage -> when (val strategy = page.strategy) {
				is RenderingStrategy.Copy -> outputWriter.writeResources(strategy.from, path)
				is RenderingStrategy.Write -> outputWriter.write(path, strategy.text, "")
				is RenderingStrategy.Callback -> outputWriter.write(path, strategy.instructions(this, page), ".html")
				is RenderingStrategy.DriLocationResolvableWrite -> outputWriter.write(
					path,
					strategy.contentToResolve { dri, sourcesets ->
						locationProvider.resolve(dri, sourcesets)
					},
					""
				)

				is RenderingStrategy.PageLocationResolvableWrite -> outputWriter.write(
					path,
					strategy.contentToResolve { pageToLocate, context ->
						locationProvider.resolve(pageToLocate, context)
					},
					""
				)

				RenderingStrategy.DoNothing -> Unit
			}

			else -> throw AssertionError(
				"Page ${page.name} cannot be rendered by renderer as it is not renderer specific nor contains content"
			)
		}
	}

	private fun buildPage(page: ContentPage): String {
		val writer = StringBuilder()

		val context = RenderingContext(
			locations = locationProvider,
			writer = writer,
			page = page,
		)

		with(context) {
			buildFrontMatter()
			buildContent(page.content)
		}

		return writer.toString()
	}
}
