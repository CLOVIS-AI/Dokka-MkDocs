plugins {
	alias(opensavvyConventions.plugins.base)
	id("dev.opensavvy.dokka-mkdocs") version "VERSION HERE"
}

dependencies {
	dokka(projects.example.exampleCore)
	dokka(projects.example.exampleApp)
}
