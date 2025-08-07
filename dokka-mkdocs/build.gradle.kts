import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask

plugins {
	`kotlin-dsl`
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
	alias(opensavvyConventions.plugins.plugin)
}

dependencies {
	implementation(libs.gradle.dokka)
}

gradlePlugin {
	plugins {
		register("dokka-mkdocs") {
			id = "dev.opensavvy.dokka-mkdocs"
			implementationClass = "opensavvy.dokka.gradle.DokkaMkDocsPlugin"
		}
	}
}

library {
	name.set("Dokka: Material for MkDocs format")
	description.set("Gradle plugin to add the Material for MkDocs format to Dokka 2+")
	homeUrl.set("https://dokka-mkdocs.opensavvy.dev/")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

val embedCurrentVersion by tasks.registering {
	description = "Embeds the current version into the source code"

	val version = project.version as String
	val file = File(kotlin.sourceSets.main.get().kotlin.srcDirs.first(), "version.kt")

	inputs.property("version", version)
	outputs.file(file)

	doLast {
		file.writeText(
			"""
				// Generated file, do not edit
				package opensavvy.dokka.gradle
				internal const val DokkaMkDocsVersion = "$version"
			""".trimIndent()
		)
	}
}

tasks.compileKotlin {
	dependsOn(embedCurrentVersion)
}

tasks.sourcesJar {
	dependsOn(embedCurrentVersion)
}

tasks.withType<DokkaGenerateTask> {
	dependsOn(embedCurrentVersion)
}
