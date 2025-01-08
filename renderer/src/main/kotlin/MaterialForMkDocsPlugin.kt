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

package opensavvy.dokka.material.mkdocs

import opensavvy.dokka.material.mkdocs.location.MarkdownLocationProvider
import opensavvy.dokka.material.mkdocs.renderer.BriefCommentPreprocessor
import opensavvy.dokka.material.mkdocs.renderer3.MkDocsRenderer3
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.renderers.PackageListCreator
import org.jetbrains.dokka.base.renderers.RootCreator
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.base.resolvers.shared.RecognizedLinkFormat
import org.jetbrains.dokka.plugability.*
import org.jetbrains.dokka.renderers.PostAction
import org.jetbrains.dokka.renderers.Renderer
import org.jetbrains.dokka.transformers.pages.PageTransformer

class MaterialForMkDocsPlugin : DokkaPlugin() {

	val mkdocsPreprocessor: ExtensionPoint<PageTransformer> by extensionPoint()

	val dokkaBase by lazy { plugin<DokkaBase>() }

	val renderer: Extension<Renderer, *, *> by extending {
		CoreExtensions.renderer providing ::MkDocsRenderer3 override dokkaBase.htmlRenderer
	}

	val locationProvider: Extension<LocationProviderFactory, *, *> by extending {
		dokkaBase.locationProviderFactory providing MarkdownLocationProvider::Factory override dokkaBase.locationProvider
	}

	val rootCreator: Extension<PageTransformer, *, *> by extending {
		mkdocsPreprocessor with RootCreator
	}

	val briefCommentPreprocessor: Extension<PageTransformer, *, *> by extending {
		mkdocsPreprocessor with BriefCommentPreprocessor()
	}

	val packageListCreator: Extension<PageTransformer, *, *> by extending {
		(mkdocsPreprocessor
			providing { PackageListCreator(it, RecognizedLinkFormat.DokkaGFM) }
			order { after(rootCreator) })
	}

	internal val alphaVersionNotifier by extending {
		CoreExtensions.postActions providing { ctx ->
			PostAction {
				ctx.logger.info(
					"The Material for MkDocs output format is still in Alpha so you may find bugs and experience migration " +
						"issues when using it. You use it at your own risk."
				)
			}
		}
	}

	@DokkaPluginApiPreview
	override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement =
		PluginApiPreviewAcknowledgement

}
