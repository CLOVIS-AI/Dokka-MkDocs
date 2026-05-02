plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.internal)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm()

	sourceSets.jvmTest.dependencies {
		implementation(libsCommon.bundles.testBalloon)
		implementation(libsCommon.opensavvy.prepared.filesystem)
		implementation(libsCommon.opensavvy.prepared.gradle)
	}
}
