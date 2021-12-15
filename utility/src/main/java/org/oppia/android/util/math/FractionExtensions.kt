package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction

/**
 * Returns a [Double] version of this fraction.
 *
 * See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L73.
 */
fun Fraction.toDouble(): Double {
  val totalParts = ((wholeNumber.toDouble() * denominator.toDouble()) + numerator.toDouble())
  val doubleVal = totalParts / denominator.toDouble()
  return if (isNegative) -doubleVal else doubleVal
}

/**
 * Returns this fraction in its most simplified form.
 *
 * See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L83.
 */
fun Fraction.toSimplestForm(): Fraction {
  val commonDenominator = gcd(numerator, denominator)
  return toBuilder().apply {
    numerator = this@toSimplestForm.numerator / commonDenominator
    denominator = this@toSimplestForm.denominator / commonDenominator
  }.build()
}

/** Returns the greatest common divisor between two integers. */
fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}
