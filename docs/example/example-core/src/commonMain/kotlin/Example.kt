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
class Example() : MyInterface {

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
	fun foo(bar: Boolean): Boolean {
		TODO()
	}

	/**
	 * This is an example function.
	 *
	 * This is a reference to a parameter: [bar].
	 *
	 * @param bar This is a parameter.
	 * @return This is a return type.
	 */
	fun foo(bar: Char): Boolean {
		TODO()
	}

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
	 *
	 * @see Example See the [Example] class for more information.
	 */
	fun foo(number: Int): Boolean {
		TODO()
	}

	/**
	 * This is a method from [MyInterface] that has been overridden by the [Example] class.
	 */
	override fun fromInterface2(): String =
		"overridden"

	/**
	 * The companion object.
	 *
	 * Second line of documentation.
	 */
	companion object {
		const val DEFAULT = 4
	}

	class Nested {

		companion object {
			const val DEFAULT = 5
		}
	}
}

/**
 * Top-level function masquerading as a constructor.
 */
fun Example(id: Int): Example {
	TODO()
}

/**
 * This is a top-level function.
 *
 * It has a long and complex documentation, with code examples and everything.
 *
 * ### Example
 *
 * ```kotlin
 * println(topLevelFunction() + 42)
 * ```
 *
 * @see Example The [Example] class is completely unrelated to this function.
 * @author OpenSavvy
 */
fun topLevelFunction(): Int {
	TODO()
}

/**
 * This is a top-level function.
 *
 * It has a long and complex documentation, with code examples and everything.
 *
 * ### Example
 *
 * ```kotlin
 * println(topLevelFunction() + 42)
 * ```
 *
 * @see Example The [Example] class is completely unrelated to this function.
 * @author OpenSavvy
 */
fun topLevelFunction(a: Int) {
	TODO()
}

/**
 * This is a top-level function.
 *
 * It has a long and complex documentation, with code examples and everything.
 *
 * ### Example
 *
 * ```kotlin
 * println(topLevelFunction() + 42)
 * ```
 *
 * @see Example The [Example] class is completely unrelated to this function.
 * @author OpenSavvy
 */
fun topLevelFunction(a: String) {
	TODO()
}

/**
 * This is another top-level function with multiple parameters.
 *
 * This a link to a method: [Example.foo].
 */
fun anotherTopLevelFunction(
	param1: String,
	param2: Int,
	param3: Boolean,
	param4: String = "default",
	onAction: () -> Unit
): Int {
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

/**
 * Extension property for [ExampleAlias].
 */
val ExampleAlias.prop2: String get() = "prop2"
