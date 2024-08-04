import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	alias(opensavvyConventions.plugins.base)
	kotlin("jvm")
	alias(opensavvyConventions.plugins.kotlin.abstractLibrary)
}

kotlin {
	jvmToolchain(8)
}

dependencies {
	compileOnly(libs.dokka.base)
	compileOnly(libs.dokka.core)
}
