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
class Example {

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
