plugins {
	alias(opensavvyConventions.plugins.base)
	kotlin("jvm")
	id("maven-publish")
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
}

java {
	withSourcesJar()
}

kotlin {
	jvmToolchain(8)
}

dependencies {
	implementation(libs.kotlinx.coroutines)
	implementation(libs.dokka.base)
	implementation(libs.dokka.core)
	implementation(libs.dokka.templating)
	implementation(libs.dokka.allModulesPage)
	implementation(projects.renderer)
}

publishing {
	publications {
		register<MavenPublication>("aggregator") {
			from(components["java"])
		}
	}
}

library {
	name.set("Dokka aggregator for Material for MkDocs")
	description.set("Dokka plugin that combines multiple modules generated with the Material for MkDocs format")
	homeUrl.set("https://dokka-mkdocs.opensavvy.dev/")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
