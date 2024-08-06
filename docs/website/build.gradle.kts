plugins {
	alias(opensavvyConventions.plugins.base)
	id("dev.opensavvy.dokkatoo-mkdocs-combine") version "VERSION HERE"
}

dependencies {
	mkdocsWebsite(projects.example.exampleCore)
}
