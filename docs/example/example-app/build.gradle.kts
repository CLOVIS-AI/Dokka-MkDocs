plugins {
	kotlin("multiplatform")
	id("dev.opensavvy.dokka-mkdocs") version "VERSION HERE"
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

dokka {
	moduleName.set("Example app")
}
