package opensavvy.dokka.material.mkdocs.test.helloworld

/**
 * This file tests link rendering.
 *
 * Links to other symbols (DRILink):
 *
 * - [HelloWorld] is a top-level constant.
 * - [simpleFunction] is a function.
 * - [textFormatting] tests text formatting.
 * - [codeBlocks] tests code blocks.
 * - [lists] tests list rendering.
 *
 * Links with custom labels:
 *
 * - [This is a custom label for HelloWorld][HelloWorld]
 * - [Click here][simpleFunction] to see the function.
 *
 * Links in code context:
 *
 * Use `[HelloWorld]` in your code to reference the constant.
 *
 * External links (ResolvedLink):
 *
 * - [Dokka documentation](https://kotlinlang.org/docs/dokka.html)
 * - [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
 * - Visit [Google](https://www.google.com) for search.
 *
 * Mixed links in a paragraph:
 *
 * You can use [HelloWorld] together with [external resources](https://example.com)
 * in the same paragraph.
 */
const val links = "test"
