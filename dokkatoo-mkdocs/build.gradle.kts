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
	homeUrl.set("https://gitlab.com/opensavvy/automation/dokka-material-mkdocs")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
