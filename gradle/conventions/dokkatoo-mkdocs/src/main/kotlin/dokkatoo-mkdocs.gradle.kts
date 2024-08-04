package opensavvy.dokka

plugins {
	base
	id("dev.adamko.dokkatoo-html")
}

pluginManager.apply(DokkatooMkDocsPlugin::class)
