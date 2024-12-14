import dev.adamko.dokkatoo.tasks.DokkatooGenerateTask
import org.gradle.kotlin.dsl.withType

plugins {
	`kotlin-dsl`
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
	alias(opensavvyConventions.plugins.plugin)
}

dependencies {
	implementation(libs.gradle.dokkatoo)
}

gradlePlugin {
	plugins {
		register("dokkatoo-mkdocs") {
			id = "dev.opensavvy.dokkatoo-mkdocs"
			implementationClass = "opensavvy.dokka.gradle.DokkatooMkDocsPlugin"
		}
	}
}

library {
	name.set("Dokkatoo: Material for MkDocs format")
	description.set("Gradle plugin to add the Material for MkDocs format to Dokkatoo")
	homeUrl.set("https://opensavvy.gitlab.io/automation/dokka-material-mkdocs/docs/")

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

tasks.withType<DokkatooGenerateTask> {
	dependsOn(embedCurrentVersion)
}
