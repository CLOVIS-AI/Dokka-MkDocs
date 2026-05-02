package opensavvy.dokka.material.mkdocs.test

import opensavvy.prepared.runner.testballoon.preparedSuite

val IntegrationTests by preparedSuite {

	val cases = findTestCases()

	test("At least one test was found") {
		check(cases.isNotEmpty()) { "No integration tests were found, maybe something went wrong?" }
	}

	for (case in cases) {
		test("Test case: ${case.name}") {
			check(case.isDirectory)
		}
	}
}
