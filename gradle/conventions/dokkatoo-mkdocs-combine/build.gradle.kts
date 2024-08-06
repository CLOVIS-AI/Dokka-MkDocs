plugins {
	`kotlin-dsl`
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
	alias(opensavvyConventions.plugins.plugin)
}

gradlePlugin {
	plugins {
		register("dokkatoo-mkdocs-combine") {
			id = "dev.opensavvy.dokkatoo-mkdocs-combine"
			implementationClass = "opensavvy.dokka.gradle.DokkatooMkDocsCombinatorPlugin"
		}
	}
}

library {
	name.set("Integrates outputs of the Material for MkDocs dokka format into a Material for MkDocs website")
	description.set("Gradle plugin to integrate Dokka into a Material for MkDocs website")
	homeUrl.set("https://gitlab.com/opensavvy/automation/dokka-material-mkdocs")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
