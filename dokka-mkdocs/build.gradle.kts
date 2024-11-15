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
	homeUrl.set("https://opensavvy.gitlab.io/automation/dokka-material-mkdocs/docs/")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
