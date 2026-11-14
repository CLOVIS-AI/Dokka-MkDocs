package opensavvy.dokka.material.mkdocs.test.helloworld

/**
 * This file tests list rendering.
 *
 * Unordered list:
 * - First item
 * - Second item
 * - Third item
 *
 * Ordered list:
 * 1. First item
 * 2. Second item
 * 3. Third item
 *
 * Nested unordered list:
 *
 * - Top level one
 *   - Nested item
 *   - Another nested item
 * - Top level two
 *   - Nested again
 *       - Deeply nested
 *
 * Nested ordered list:
 *
 * 1. First
 *    1. Nested first
 *    2. Nested second
 * 2. Second
 *
 * Mixed nested lists:
 *
 * - Unordered parent
 *   1. Ordered child one
 *   2. Ordered child two
 * - Another unordered parent
 *     - Back to unordered
 *     - And another
 *
 * Ordered list with unordered children:
 *
 * 1. Step one
 *    - Sub-item a
 *    - Sub-item b
 * 2. Step two
 *    - Sub-item c
 */
const val lists = "test"
