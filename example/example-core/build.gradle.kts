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

// Workaround to use the module from this repository instead of using the one that is
// published. In a real project using the plugin, do not copy this!
configurations.all {
	resolutionStrategy.dependencySubstitution {
		substitute(module("dev.opensavvy.dokka.mkdocs:renderer")).using(project(":renderer"))
	}
}

dokkatoo {
	dokkatooSourceSets.configureEach {
		this.displayName.set("Example Core")
	}
}
