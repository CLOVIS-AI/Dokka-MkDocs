package opensavvy.dokka.material.mkdocs.example

/**
 * This class is an example to see how the documentation website is generated.
 *
 * ### Example
 *
 * ```kotlin
 * Example().foo("Hello") shouldBe "Hello world!"
 * ```
 *
 * Another example:
 * ```java
 * class Foo {
 *     public static void main(String[] args) {
 *         System.out.println("Hello world!" + 3 + true);
 *     }
 * }
 * ```
 *
 * @constructor Public constructor for the [Example] class.
 */
class Example() {

	/**
	 * A value that increments by one each time it is accessed.
	 *
	 * This builder is not thread-safe.
	 */
	var counter: Int = 0
		get() = field++
		private set

	/**
	 * Secondary constructor.
	 *
	 * @param s Some variable.
	 */
	constructor(s: String) : this()

	/**
	 * This is an example function.
	 *
	 * This is a reference to a parameter: [bar].
	 *
	 * @param bar This is a parameter.
	 * @return This is a return type.
	 */
	fun foo(bar: String): Boolean {
		TODO()
	}

	/**
	 * Some other documentation.
	 */
	fun foo(number: Int): Boolean {
		TODO()
	}
}

/**
 * This is a top-level function.
 */
fun topLevelFunction(): Int {
	TODO()
}

/**
 * This is a typealias.
 *
 * I don't really know what to write here, but it needs to spend multiple paragraphs.
 *
 * Learn more about typealiases in the [official documentation](https://kotlinlang.org/docs/type-aliases.html).
 */
typealias ExampleAlias = Example

/**
 * Extension function for [ExampleAlias].
 */
@Suppress("RedundantSuspendModifier")
suspend inline fun <reified T> ExampleAlias.foo2(): T = TODO()
