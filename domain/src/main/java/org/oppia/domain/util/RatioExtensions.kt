package org.oppia.domain.util

import org.oppia.app.model.RatioExpression

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
