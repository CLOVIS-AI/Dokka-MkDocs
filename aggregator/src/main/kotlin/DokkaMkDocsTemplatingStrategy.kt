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

import opensavvy.dokka.material.mkdocs.GfmCommand
import opensavvy.dokka.material.mkdocs.GfmCommand.Companion.command
import opensavvy.dokka.material.mkdocs.GfmCommand.Companion.label
import opensavvy.dokka.material.mkdocs.GfmCommand.Companion.templateCommandRegex
import opensavvy.dokka.material.mkdocs.ResolveLinkGfmCommand
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.allModulesPage.AllModulesPagePlugin
import org.jetbrains.dokka.base.templating.parseJson
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.templates.TemplateProcessingStrategy
import java.io.BufferedWriter
import java.io.File

class DokkaMkDocsTemplatingStrategy(
	val context: DokkaContext
) : TemplateProcessingStrategy {

	private val externalModuleLinkResolver =
		context.plugin<AllModulesPagePlugin>().querySingle { externalModuleLinkResolver }

	override fun process(input: File, output: File, moduleContext: DokkaConfiguration.DokkaModuleDescription?): Boolean =
		if (input.isFile && input.extension == "md") {
			input.bufferedReader().use { reader ->
				//This should also work whenever we have a misconfigured dokka and output is pointing to the input
				//the same way that html processing does
				if (input.absolutePath == output.absolutePath) {
					context.logger.info("Attempting to process MkDocs templates in place for directory $input, this suggests miss configuration.")
					val lines = reader.readLines()
					output.bufferedWriter().use { writer ->
						lines.forEach { line ->
							writer.processAndWrite(line, output)
						}
					}
				} else {
					output.bufferedWriter().use { writer ->
						reader.lineSequence().forEach { line ->
							writer.processAndWrite(line, output)
						}
					}
				}
			}
			true
		} else false

	private fun BufferedWriter.processAndWrite(line: String, output: File) =
		processLine(line, output).run {
			write(this)
			newLine()
		}

	private fun processLine(line: String, output: File): String =
		line.replace(templateCommandRegex) {
			when (val command = parseJson<GfmCommand>(it.command)) {
				is ResolveLinkGfmCommand -> resolveLink(output, command.dri, it.label)
			}
		}

	private fun resolveLink(fileContext: File, dri: DRI, label: String): String =
		externalModuleLinkResolver.resolve(dri, fileContext)?.let { address ->
			"[$label]($address)"
		} ?: label
}
