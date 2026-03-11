rootProject.name = "docs"

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}

	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
		create("libsCommon") {
			from(files("../gradle/common.versions.toml"))
		}
	}
}

pluginManagement {
	repositories {
		// region OpenSavvy Conventions

		maven {
			name = "opensavvy-gradle-conventions"
			url = uri("https://gitlab.com/api/v4/projects/51233470/packages/maven")

			metadataSources {
				gradleMetadata()
				mavenPom()
			}

			content {
				@Suppress("UnstableApiUsage")
				includeGroupAndSubgroups("dev.opensavvy")
			}
		}

		// endregion
		// region Standard repositories

		gradlePluginPortal()
		google()
		mavenCentral()

		// endregion
	}

	includeBuild("..")
}

plugins {
	id("dev.opensavvy.conventions.settings") version "2.3.1"
}

includeBuild("..")

include(
	"website",
	"example:example-core",
	"example:example-app",
)
