package opensavvy.dokka.material.mkdocs.test

import opensavvy.prepared.compat.filesystem.div
import java.io.File

fun findTestCases(): List<TestCase> {
	val srcFolder = File("../test-data/src").absoluteFile
	check(srcFolder.exists())
	check(srcFolder.isDirectory)

	val platformFolders = srcFolder.listFiles()
	checkNotNull(platformFolders) { "No platform folders found in test data source" }

	val testCases = platformFolders
		.map { File(it, "kotlin") }
		.flatMap { kotlinFolder ->
			kotlinFolder.findMarkdownRecursively().map {  child ->
				TestCase(child.absoluteFile, child.relativeTo(kotlinFolder))
			}
		}
	check(testCases.isNotEmpty()) { "No test cases were found" }

	return testCases
}

data class TestCase(
	val absolute: File,
	val relative: File,
)

private fun File.findMarkdownRecursively(): List<File> {
	if (!isDirectory)
		return emptyList()

	val files = listFiles()
		?: return emptyList()

	return files.filter { it.isFile && it.extension == "md" } +
		files.filter { it.isDirectory }.flatMap { it.findMarkdownRecursively() }
}

fun File.root(): File =
	parentFile?.root() ?: this

fun File.replaceRoot(by: File): File =
	if (parentFile == null) by
	else parentFile!!.replaceRoot(by) / this.name
