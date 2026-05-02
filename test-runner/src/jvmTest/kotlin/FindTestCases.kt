package opensavvy.dokka.material.mkdocs.test

import java.io.File

fun findTestCases(): List<File> {
	val srcFolder = File("../test-data/src").absoluteFile
	check(srcFolder.exists())
	check(srcFolder.isDirectory)

	val platformFolders = srcFolder.listFiles()
	checkNotNull(platformFolders) { "No platform folders found in test data source" }

	val commonMain = platformFolders.firstOrNull { it.name == "commonMain" }
	checkNotNull(commonMain) { "No commonMain folder found in test data source: ${srcFolder}/commonMain" }

	val testCases = platformFolders
		.map { File(it, "kotlin") }
		.flatMap {
			it.listFiles()?.asList().orEmpty()
		}
		.filter { it.isDirectory }
		.distinctBy { it.name }
	check(testCases.isNotEmpty()) { "No test cases were found" }

	return testCases
}
