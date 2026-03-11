plugins {
	kotlin("multiplatform")
	id("dev.opensavvy.dokka-mkdocs")
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
}

dokka {
	moduleName.set("Library module")

	dokkaSourceSets.configureEach {
		includes.from("README.md")
	}
}
