package org.oppia.domain.util

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
