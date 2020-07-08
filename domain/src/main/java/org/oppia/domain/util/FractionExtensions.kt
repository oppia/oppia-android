package org.oppia.domain.util

import org.oppia.app.model.Fraction

/**
 * Returns a float version of this fraction.
 *
 * See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L73.
 */
fun Fraction.toFloat(): Float {
  val totalParts = ((wholeNumber * denominator) + numerator).toFloat()
  val floatVal = totalParts / denominator.toFloat()
  return if (isNegative) -floatVal else floatVal
}

/**
 * Returns this fraction in its most simplified form.
 *
 * See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L83.
 */
fun Fraction.toSimplestForm(): Fraction {
  val commonDenominator = gcd(numerator, denominator)
  return toBuilder().setNumerator(numerator / commonDenominator)
    .setDenominator(denominator / commonDenominator).build()
}

/** Returns the greatest common divisor between two integers. */
private fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}
