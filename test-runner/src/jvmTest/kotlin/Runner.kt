package opensavvy.dokka.material.mkdocs.test

import opensavvy.prepared.compat.filesystem.div
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.config.CoroutineTimeout
import java.io.File
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
		test("Test case: ${case.relative}") {
			val generated = generateDocs()

			val packageCase = "opensavvy.dokka.material.mkdocs.test.${case.relative.root().name}"
			println("» Package:   $packageCase")

			val generatedRelativePath = case.relative.replaceRoot(File(packageCase))
			println("» Relative:  ${case.relative}")
			println("             $generatedRelativePath")

			val outputDirectory = generated / "test-data"
			check(outputDirectory.isDirectory) { "No output directory was generated for test case" }
			val actualFile = outputDirectory.resolve(case.relative.replaceRoot(File(packageCase)))

			println("» Output:    file://${outputDirectory.absolutePath}")
			println("» Expected:  file://${case.absolute}")
			println("» Generated: file://${actualFile.absolutePath}")

			val expected = case.absolute.readText()
			val actual = actualFile.readText()

			assertEquals(
				expected = expected,
				actual = actual,
			)
		}
	}
}
