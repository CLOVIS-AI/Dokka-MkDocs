package opensavvy.dokka.material.mkdocs.example.sub1

import opensavvy.dokka.material.mkdocs.example.ExampleAlias

/**
 * Extension function for [ExampleAlias].
 *
 * This extension is not in the same package as [ExampleAlias], so it should remain as a standalone page.
 */
@Suppress("RedundantSuspendModifier")
suspend inline fun <reified T> ExampleAlias.foo3(): T = TODO()

/**
 * Extension property for [ExampleAlias].
 *
 * This extension is not in the same package as [ExampleAlias], so it should remain as a standalone page.
 */
val ExampleAlias.prop3: String get() = "prop3"
