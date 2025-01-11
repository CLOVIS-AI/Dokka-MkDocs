plugins {
	alias(opensavvyConventions.plugins.base)
	id("dev.opensavvy.dokka-mkdocs")
}

dependencies {
	dokka(projects.example.exampleCore)
	dokka(projects.example.exampleApp)
}
