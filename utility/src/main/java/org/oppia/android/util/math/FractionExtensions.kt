package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import kotlin.math.absoluteValue

/**
 * Returns a submittable answer string representation of this fraction (note that this may not be
 * the verbatim string originally submitted by the user, if any.
 */
fun Fraction.toAnswerString(): String {
  return when {
    isOnlyWholeNumber() -> {
      // Fraction is only a whole number.
      if (isNegative) "-$wholeNumber" else "$wholeNumber"
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
 * Double version of [toFloat] (note that this doesn't actually guarantee additional precision over
 * toFloat().
 */
fun Fraction.toDouble(): Double = toFloat().toDouble()

/**
 * Returns this fraction in its most simplified form.
 *
 * See: https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L83.
 */
fun Fraction.toSimplestForm(): Fraction {
  val commonDenominator = gcd(numerator, denominator)
  return toBuilder()
      .setWholeNumber(wholeNumber)
      .setNumerator(numerator / commonDenominator)
    .setDenominator(denominator / commonDenominator)
      .build()
}

/**
 * Returns this fraction in an improper form (that is, with a 0 whole number and only fractional
 * parts).
 */
fun Fraction.toImproperForm(): Fraction {
  return toBuilder().setNumerator(numerator + (denominator * wholeNumber)).setWholeNumber(0).build()
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
    it.toBuilder()
        .setWholeNumber(it.wholeNumber + (it.numerator / it.denominator))
        .setNumerator(it.numerator % it.denominator)
        .build()
  }
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
  return Fraction.newBuilder()
      .setIsNegative(isNegative)
      .setNumerator(newNumerator)
      .setDenominator(commonDenominator)
      .build()
      .toProperForm()
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
  return Fraction.newBuilder()
      .setIsNegative(isNegative)
      .setNumerator(newNumerator)
      .setDenominator(newDenominator)
      .build()
      .toProperForm()
}

/** Returns the proper form of the division from this fraction by the specified fraction. */
operator fun Fraction.div(rhs: Fraction): Fraction {
  // a / b = a * b^-1 (b's inverse).
  return this * rhs.toInvertedImproperForm()
}

/** Returns the inverse improper fraction representation of this fraction. */
private fun Fraction.toInvertedImproperForm(): Fraction {
  val improper = toImproperForm()
  return improper.toBuilder()
      .setNumerator(improper.denominator)
      .setDenominator(improper.numerator)
      .build()
}

/** Returns the negated form of this fraction. */
operator fun Fraction.unaryMinus(): Fraction {
  return toBuilder().setIsNegative(!isNegative).build()
}

/** Returns the greatest common divisor between two integers. */
fun gcd(x: Int, y: Int): Int {
  return if (y == 0) x else gcd(y, x % y)
}

/** Returns the least common multiple between two integers. */
fun lcm(x: Int, y: Int): Int {
  // Reference: https://en.wikipedia.org/wiki/Least_common_multiple#Calculation.
  return (x * y).absoluteValue / gcd(x, y)
}
