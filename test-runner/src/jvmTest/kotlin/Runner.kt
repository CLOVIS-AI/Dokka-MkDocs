package opensavvy.dokka.material.mkdocs.test

import opensavvy.prepared.compat.filesystem.div
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

val IntegrationTests by preparedSuite {

	val cases = findTestCases()

	test("At least one test was found") {
		check(cases.isNotEmpty()) { "No integration tests were found, maybe something went wrong?" }
	}

	test("Generate the documentation", CoroutineTimeout(10.minutes)) {
		val docs = generateDocs()

		check(docs.isDirectory) { "No documentation directory was generated." }
		check(docs.listFiles() != null) { "No documentation files were generated." }
	}

	for (case in cases) {
		test("Test case: ${case.name}") {
			check(case.isDirectory)

			val outputDirectory = generateDocs() / "test-data" / "opensavvy.dokka.material.mkdocs.test.${case.name}"
			check(outputDirectory.isDirectory) { "No output directory was generated for test case" }
			println("» Generated in: ${outputDirectory.absolutePath}")

			val markdownFiles = case.listFiles { _, name -> name.endsWith(".md") }

			for (file in markdownFiles) {
				println("» Checking: ${file.name}")

				val expected = file.readText()
				val actualFile = outputDirectory / file.name
				val actual = actualFile.readText()

				assertEquals(
					expected = expected,
					actual = actual,
					message = "Found a difference between the reference file file://${file.absolutePath} and the generated file file://${actualFile.absolutePath}"
				)
			}
		}
	}
}
