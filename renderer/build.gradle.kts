plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()

	sourceSets.jvmMain.dependencies {
		implementation(libs.kotlinx.coroutines)
		compileOnly(libs.dokka.base)
		compileOnly(libs.dokka.core)
		compileOnly(libs.dokka.templating)
		compileOnly(libs.dokka.allModulesPage)
	}

}

tapmoc {
	java(libs.versions.java.lowestGradle.get().toInt())
}

library {
	name.set("Dokka renderer for Material for MkDocs")
	description.set("Dokka plugin that adds the Material for MkDocs format")
	homeUrl.set("https://dokka-mkdocs.opensavvy.dev/")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
