package opensavvy.dokka.material.mkdocs.example

/**
 * Simple interface.
 */
interface MyInterface {

	/**
	 * This is a method from the interface [MyInterface].
	 *
	 * It is not overridden by the class [Example].
	 */
	fun fromInterface1(): String {
		return "initial"
	}

	/**
	 * This is a method from the interface [MyInterface].
	 *
	 * It is override by the class [Example].
	 */
	fun fromInterface2(): String
}

/**
 * A fake constructor that instantiates a [MyInterface] instance.
 *
 * ### Implementation details
 *
 * It uses the [Example] class.
 */
fun MyInterface(): MyInterface =
	Example()
