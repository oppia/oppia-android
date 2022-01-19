package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction

/** Returns whether this fraction has a fractional component. */
fun Fraction.hasFractionalPart(): Boolean {
  return numerator != 0
}

/**
 * Returns whether this fraction only represents a whole number. Note that for the fraction '0' this
 * will return true.
 */
fun Fraction.isOnlyWholeNumber(): Boolean {
  return !hasFractionalPart()
}

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
 * Returns a submittable answer string representation of this fraction (note that this may not be
 * the verbatim string originally submitted by the user, if any.
 */
fun Fraction.toAnswerString(): String {
  return when {
    // Fraction is only a whole number.
    isOnlyWholeNumber() -> when (wholeNumber) {
      0 -> "0" // 0 is always 0 regardless of its negative sign.
      else -> if (isNegative) "-$wholeNumber" else "$wholeNumber"
    }
    wholeNumber == 0 -> {
      // Fraction contains just a fraction (no whole number).
      when (denominator) {
        1 -> if (isNegative) "-$numerator" else "$numerator"
        else -> if (isNegative) "-$numerator/$denominator" else "$numerator/$denominator"
      }
    }
    else -> {
      // Otherwise it's a mixed number. Note that the denominator is always shown here to account
      // for strange cases that would require evaluation to resolve, such as: "2 2/1".
      if (isNegative) {
        "-$wholeNumber $numerator/$denominator"
      } else {
        "$wholeNumber $numerator/$denominator"
      }
    }
  }
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

/**
 * Returns this fraction in an improper form (that is, with a 0 whole number and only fractional
 * parts).
 */
fun Fraction.toImproperForm(): Fraction {
  val newNumerator = numerator + (denominator * wholeNumber)
  return toBuilder().apply {
    numerator = newNumerator
    wholeNumber = 0
  }.build()
}

/** Returns the negated form of this fraction. */
operator fun Fraction.unaryMinus(): Fraction {
  return toBuilder().apply { isNegative = !this@unaryMinus.isNegative }.build()
}

/** Returns the greatest common divisor between two integers. */
private fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}
