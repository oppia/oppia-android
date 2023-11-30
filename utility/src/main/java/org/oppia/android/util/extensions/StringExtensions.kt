package org.oppia.android.util.extensions

/**
 * Normalizes whitespace in the specified string in a way consistent with Oppia web:
 * https://github.com/oppia/oppia/blob/392323/core/templates/dev/head/filters/string-utility-filters/normalize-whitespace.filter.ts.
 */
fun String.normalizeWhitespace(): String {
  return trim().replace("\\s{2,}".toRegex(), " ")
}

/**
 * Removes whitespace in the specified string after [nomralizeWhitespace] has removed extra spaces
 * from the string.
 */
fun String.removeWhitespace(): String {
  return this.normalizeWhitespace().replace(" ", "")
}

/**
 *Checks if the string contains a placeholder in the form of "%s" or "%1$s".
 * The placeholder may include an optional number followed by a '$' symbol.
 * @return `true` if the string contains a placeholder, `false` otherwise.
 */
fun String.containsPlaceholderRegex(): Boolean {
  val placeholderRegex = Regex("""%([0-9]+\$)?[a-zA-Z]""")
  return placeholderRegex.containsMatchIn(this)
}
