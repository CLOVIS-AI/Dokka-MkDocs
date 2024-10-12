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
	compileOnly(libs.dokka.base)
	compileOnly(libs.dokka.core)
}

publishing {
	publications {
		register<MavenPublication>("renderer") {
			from(components["java"])
		}
	}
}

library {
	name.set("Dokka render for Material for MkDocs")
	description.set("Dokka plugin that adds the Material for MkDocs format")
	homeUrl.set("https://gitlab.com/opensavvy/automation/dokka-material-mkdocs")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
