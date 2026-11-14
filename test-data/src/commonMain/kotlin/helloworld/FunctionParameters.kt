package opensavvy.dokka.material.mkdocs.test.helloworld

/**
 * Tests for function parameter rendering.
 *
 * This class covers various types of function parameters:
 *
 * - Normal parameters
 * - Parameters with default values
 * - Vararg parameters
 * - Lambda parameters (function types)
 * - Lambda parameters with receiver
 * - Lambda parameters with noinline modifier
 * - Lambda parameters with crossinline modifier
 * - Reified type parameters
 */
class FunctionParameters {

	/**
	 * A function with normal parameters.
	 */
	fun normalParameters(name: String, age: Int): String = error("")

	/**
	 * A function with parameters that have default values.
	 */
	fun defaultParameters(host: String = "localhost", port: Int = 8080): String = error("")

	/**
	 * A function with a vararg parameter.
	 */
	fun varargParameters(vararg items: String): String = error("")

	/**
	 * A function with vararg and a trailing lambda.
	 */
	fun varargWithLambda(vararg items: Int, action: (Int) -> Unit): Nothing {
		error("")
	}

	/**
	 * A function with a lambda parameter.
	 */
	fun lambdaParameter(action: () -> Unit): Unit = error("")

	/**
	 * A function with a lambda parameter that takes arguments.
	 */
	fun lambdaWithArguments(transform: (String) -> String): String = error("")

	/**
	 * A function with a lambda parameter with receiver.
	 */
	fun lambdaWithReceiver(builder: StringBuilder.() -> Unit): String = error("")

	/**
	 * A function with multiple lambda parameters.
	 */
	fun multipleLambdas(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
		error("")
	}

	/**
	 * An inline function with noinline lambda parameter.
	 */
	inline fun noinlineLambda(onComplete: () -> Unit, noinline onCancel: () -> Unit) {
		error("")
	}

	/**
	 * An inline function with crossinline lambda parameter.
	 */
	inline fun crossinlineLambda(crossinline action: () -> Unit) {
		error("")
	}

	/**
	 * An inline function with reified type parameter.
	 */
	inline fun <reified T> reifiedTypeParameter(value: Any): Boolean = error("")

	/**
	 * An inline function with multiple reified type parameters.
	 */
	inline fun <reified T, reified U> multipleReified(first: Any, second: Any): Boolean = error("")

	/**
	 * A function with mixed parameter types.
	 */
	fun mixedParameters(
		name: String,
		count: Int = 10,
		vararg tags: String,
		onComplete: (String) -> Unit,
	) {
		error("")
	}

	/**
	 * An inline function with mixed lambda modifiers.
	 */
	inline fun mixedLambdaModifiers(
		crossinline required: () -> Unit,
		noinline optional: () -> Unit = {},
		fallback: () -> Unit = {},
	) {
		error("")
	}
}
