plugins {
	kotlin("multiplatform")
	id("dev.opensavvy.dokkatoo-mkdocs") version "VERSION HERE"
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}

	sourceSets.commonMain.dependencies {
		implementation(projects.example.exampleCore)
	}
}
