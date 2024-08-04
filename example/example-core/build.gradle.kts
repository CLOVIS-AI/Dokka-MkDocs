plugins {
	kotlin("multiplatform")
	id("org.jetbrains.dokka") version libs.versions.dokka
}

dependencies {
	dokkaPlugin(projects.renderer)
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
}
