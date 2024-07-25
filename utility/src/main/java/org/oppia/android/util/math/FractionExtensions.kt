package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import kotlin.math.abs
import kotlin.math.absoluteValue

/** Returns whether this fraction has a fractional component. */
fun Fraction.hasFractionalPart(): Boolean {
  return numerator != 0
}

/**
 * Returns whether this fraction only represents a whole number.
 *
 * Note that for the fraction '0' this will return true. Furthermore, this will return false for
 * whole number-like improper fractions such as '3/1'.
 */
fun Fraction.isOnlyWholeNumber(): Boolean {
  return !hasFractionalPart()
}

/**
 * Returns this fraction as a whole number. Note that this will not return a value that is
 * mathematically equivalent to this fraction unless [isOnlyWholeNumber] returns true.
 */
fun Fraction.toWholeNumber(): Int = if (isNegative) -wholeNumber else wholeNumber

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
 * Returns this fraction in its proper form by first converting to simplest denominator, then
 * extracting a whole number component.
 *
 * This function will properly convert a fraction whose denominator is 1 into a whole number-only
 * fraction.
 */
fun Fraction.toProperForm(): Fraction {
  return toSimplestForm().let {
    it.toBuilder().apply {
      wholeNumber = it.wholeNumber + (it.numerator / it.denominator)
      numerator = it.numerator % it.denominator
    }.build()
  }
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

/** Returns the inverse improper fraction representation of this fraction. */
private fun Fraction.toInvertedImproperForm(): Fraction {
  return toImproperForm().let { improper ->
    improper.toBuilder().apply {
      numerator = improper.denominator
      denominator = improper.numerator
    }.build()
  }
}

/** Returns the negated form of this fraction. */
operator fun Fraction.unaryMinus(): Fraction {
  return toBuilder().apply { isNegative = !this@unaryMinus.isNegative }.build()
}

/** Adds two fractions together and returns a new one in its proper form. */
operator fun Fraction.plus(rhs: Fraction): Fraction {
  // First, eliminate the whole number by computing improper fractions.
  val leftFraction = toImproperForm()
  val rightFraction = rhs.toImproperForm()

  // Second, find a common denominator and compute the new numerators.
  val commonDenominator = lcm(leftFraction.denominator, rightFraction.denominator)
  val leftFactor = commonDenominator / leftFraction.denominator
  val rightFactor = commonDenominator / rightFraction.denominator
  val leftNumerator = leftFraction.numerator * leftFactor
  val rightNumerator = rightFraction.numerator * rightFactor

  // Third, determine how the numerators are combined (based on negatives) and whether the result is
  // negative.
  val leftNeg = leftFraction.isNegative
  val rightNeg = rightFraction.isNegative
  val (newNumerator, isNegative) = when {
    leftNeg && rightNeg -> leftNumerator + rightNumerator to true
    !leftNeg && !rightNeg -> leftNumerator + rightNumerator to false
    leftNeg && !rightNeg ->
      (-leftNumerator + rightNumerator).absoluteValue to (leftNumerator > rightNumerator)
    !leftNeg && rightNeg ->
      (leftNumerator - rightNumerator).absoluteValue to (rightNumerator > leftNumerator)
    else -> throw Exception("Impossible case")
  }

  // Finally, compute the new fraction and convert it to proper form to compute its whole number.
  return Fraction.newBuilder().apply {
    this.isNegative = isNegative
    numerator = newNumerator
    denominator = commonDenominator
  }.build().toProperForm()
}

/**
 * Subtracts the specified fraction from this fraction and returns the result in its proper form.
 */
operator fun Fraction.minus(rhs: Fraction): Fraction {
  // a - b = a + -b
  return this + -rhs
}

/** Multiples this fraction by the specified and returns the result in its proper form. */
operator fun Fraction.times(rhs: Fraction): Fraction {
  // First, convert both fractions into their improper forms.
  val leftFraction = toImproperForm()
  val rightFraction = rhs.toImproperForm()

  // Second, multiple the numerators and denominators piece-wise.
  val newNumerator = leftFraction.numerator * rightFraction.numerator
  val newDenominator = leftFraction.denominator * rightFraction.denominator

  // Third, determine negative (negative is retained if only one is negative).
  val isNegative = leftFraction.isNegative xor rightFraction.isNegative
  return Fraction.newBuilder().apply {
    this.isNegative = isNegative
    numerator = newNumerator
    denominator = newDenominator
  }.build().toProperForm()
}

/** Returns the proper form of the division from this fraction by the specified fraction. */
operator fun Fraction.div(rhs: Fraction): Fraction {
  // a / b = a * b^-1 (b's inverse).
  return this * rhs.toInvertedImproperForm()
}

/**
 * Raises this [Fraction] to the specified [exp] power and returns the result.
 *
 * Note that since this is an infix operation it should be used as follows (as an example):
 * ```kotlin
 * val result = fraction pow integerPower
 * ```
 *
 * This function can only fail when (exceptions are thrown in all cases):
 * - This [Fraction] is malformed or incomplete (e.g. a default instance).
 * - The resulting [Fraction] would result in a zero denominator.
 *
 * Some specific details about the returned value:
 * - A proper-form fraction is always returned (per [toProperForm]).
 * - Negative powers are supported (they will invert the resulting fraction).
 * - 0^0 is special-cased to return a 1-valued fraction for consistency with the power function for
 *   reals (see that KDoc and/or https://stackoverflow.com/a/19955996 for context).
 */
infix fun Fraction.pow(exp: Int): Fraction {
  return when {
    exp == 0 -> {
      Fraction.newBuilder().apply {
        wholeNumber = 1
        denominator = 1
      }.build()
    }
    exp == 1 -> this
    // x^-2 == 1/(x^2).
    exp < 1 -> (this pow -exp).toInvertedImproperForm().toProperForm()
    else -> { // i > 1
      var newValue = this
      for (i in 1 until exp) newValue *= this
      return newValue.toProperForm()
    }
  }
}

/** Returns the [Fraction] representation of this integer (as a whole number fraction). */
fun Int.toWholeNumberFraction(): Fraction {
  val intValue = this
  return Fraction.newBuilder().apply {
    isNegative = intValue < 0
    wholeNumber = abs(intValue)
    numerator = 0
    denominator = 1
  }.build()
}

/** Returns the greatest common divisor between two integers. */
private fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}

/** Returns the least common multiple between two integers. */
private fun lcm(x: Int, y: Int): Int {
  // Reference: https://en.wikipedia.org/wiki/Least_common_multiple#Calculation.
  return (x * y).absoluteValue / gcd(x, y)
}
