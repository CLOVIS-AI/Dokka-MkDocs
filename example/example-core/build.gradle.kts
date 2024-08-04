plugins {
	kotlin("multiplatform")
	id("opensavvy.dokka.dokkatoo-mkdocs") version "VERSION HERE"
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
}
