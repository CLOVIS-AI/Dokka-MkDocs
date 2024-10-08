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
}

dokkatoo {
	moduleName.set("Library module")

	dokkatooSourceSets.configureEach {
		includes.from("README.md")
	}
}
