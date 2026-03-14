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

import opensavvy.dokka.material.mkdocs.renderer3.DeferredLinkCommand
import opensavvy.dokka.material.mkdocs.renderer3.DeferredLinkCommand.Companion.deferredCommandRegex
import opensavvy.dokka.material.mkdocs.renderer3.DeferredLinkCommand.Companion.deferredLinkCommand
import opensavvy.dokka.material.mkdocs.renderer3.DeferredLinkCommand.Companion.deferredLinkLabel
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.allModulesPage.AllModulesPagePlugin
import org.jetbrains.dokka.base.templating.parseJson
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.templates.TemplateProcessingStrategy
import java.io.File

class DokkaMkDocsTemplatingStrategy(
	context: DokkaContext
) : TemplateProcessingStrategy {

	private val externalModuleLinkResolver =
		context.plugin<AllModulesPagePlugin>().querySingle { externalModuleLinkResolver }

	override fun process(input: File, output: File, moduleContext: DokkaConfiguration.DokkaModuleDescription?): Boolean =
		if (input.isFile && input.extension == "md") {
			val processed = input.readText().replace(deferredCommandRegex) { match ->
				val command = parseJson<DeferredLinkCommand>(match.deferredLinkCommand)
				externalModuleLinkResolver.resolve(command.dri, output)?.let { address ->
					command.format(address, match.deferredLinkLabel)
				} ?: match.deferredLinkLabel
			}

			output.parentFile?.mkdirs()
			output.writeText(processed)
			true
		} else {
			false
		}
}
