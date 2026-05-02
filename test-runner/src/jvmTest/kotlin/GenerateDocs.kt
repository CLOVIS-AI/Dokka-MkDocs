package opensavvy.dokka.material.mkdocs.test

import opensavvy.prepared.compat.filesystem.div
import opensavvy.prepared.suite.shared
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files

val generateDocs by shared {
	val workDir = Files.createTempDirectory("dokka-gradle-test").toFile()
	println("Working directory: $workDir")

	val originalWorkDir = File("..")

	(originalWorkDir / "test-data").copyRecursively(workDir / "test-data")
	(originalWorkDir / "gradle").copyRecursively(workDir / "gradle")

	(workDir / "settings.gradle.kts").writeText("""
		rootProject.name = "example-test"
		
		dependencyResolutionManagement {
			repositories {
				mavenCentral()
			}
			
			versionCatalogs {
				create("libsCommon") {
					from(files("gradle/common.versions.toml"))
				}
			}
		}
		
		include("test-data")
		include("website")
		includeBuild("${File("..").absoluteFile}")
	""".trimIndent())

	(workDir / "build.gradle.kts").writeText("""
		plugins {
			kotlin("multiplatform") version libsCommon.versions.kotlin.get() apply false
		}
	""".trimIndent())

	(workDir / "website").mkdir()
	(workDir / "website" / "build.gradle.kts").writeText("""
		plugins {
			id("dev.opensavvy.dokka-mkdocs")
		}
		
		dependencies {
			dokka(project(":test-data"))
		}
	""".trimIndent())

	GradleRunner.create()
		.withGradleVersion("9.4.1")
		.withProjectDir(workDir)
		.withArguments(":website:dokkaCopyIntoMkDocs")
		.build()

	workDir / "website" / "docs" / "api"
}
