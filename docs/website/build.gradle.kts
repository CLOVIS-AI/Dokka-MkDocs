plugins {
	alias(opensavvyConventions.plugins.base)
	id("dev.opensavvy.dokkatoo-mkdocs") version "VERSION HERE"
}

dependencies {
	dokkatoo(projects.example.exampleCore)
}
