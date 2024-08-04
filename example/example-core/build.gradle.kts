plugins {
	kotlin("multiplatform")
	id("org.jetbrains.dokka") version libs.versions.dokka
}

dependencies {
	dokkaPlugin(projects.plugin)
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
}
