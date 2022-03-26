package org.oppia.android.util.math

import org.oppia.android.app.model.RatioExpression

/**
 * Returns this Ratio in its most simplified form.
 */
fun RatioExpression.toSimplestForm(): List<Int> {
  return if (this.ratioComponentList.contains(0)) {
    this.ratioComponentList
  } else {
    val gcdComponentResult = this.ratioComponentList.reduce { x, y -> gcd(x, y) }
    this.ratioComponentList.map { x -> x / gcdComponentResult }
  }
}

/**
 * Returns this Ratio in string format.
 * E.g. [1, 2, 3] will yield to 1:2:3
 */
fun RatioExpression.toAnswerString(): String {
  return ratioComponentList.joinToString(separator = ":")
}

/** Returns the greatest common divisor between two integers. */
private fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}
