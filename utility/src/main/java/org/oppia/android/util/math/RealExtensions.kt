package org.oppia.android.util.math

import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET

/**
 * Returns whether this [Real] is explicitly a rational type (i.e. a fraction).
 *
 * This returns false if the real is an integer despite that being mathematically rational.
 */
fun Real.isRational(): Boolean = realTypeCase == RATIONAL

/** Returns whether this [Real] is negative. */
fun Real.isNegative(): Boolean = when (realTypeCase) {
  RATIONAL -> rational.isNegative
  IRRATIONAL -> irrational < 0
  INTEGER -> integer < 0
  REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $this.")
}

/**
 * Returns a [Double] representation of this [Real] that is approximately the same value (per
 * [isApproximatelyEqualTo]).
 */
fun Real.toDouble(): Double {
  return when (realTypeCase) {
    RATIONAL -> rational.toDouble()
    INTEGER -> integer.toDouble()
    IRRATIONAL -> irrational
    REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $this.")
  }
}

/**
 * Returns a human-readable, plaintext representation of this [Real].
 *
 * Note that the returned value is guaranteed to be a self-contained numeric expression representing
 * the real (which means proper fractions are converted to improper answer strings since fractions
 * like '1 1/2' can't be written as a numeric expression without converting them to an improper
 * form: '3/2').
 *
 * Note that this will return an empty string if this [Real] doesn't represent an actual real value
 * (e.g. a default instance).
 */
fun Real.toPlainText(): String = when (realTypeCase) {
  // Note that the rational part is first converted to an improper fraction since mixed fractions
  // can't be expressed as a single coefficient in typical polynomial syntax).
  RATIONAL -> rational.toImproperForm().toAnswerString()
  IRRATIONAL -> irrational.toPlainString()
  INTEGER -> integer.toString()
  // The Real type isn't valid, so rather than failing just return an empty string.
  REALTYPE_NOT_SET, null -> ""
}

/**
 * Returns whether this [Real] is approximately equal to the specified [Double] per
 * [Double.approximatelyEquals].
 */
fun Real.isApproximatelyEqualTo(value: Double): Boolean {
  return toDouble().approximatelyEquals(value)
}

/**
 * Returns a negative version of this [Real] such that the original real plus the negative version
 * would result in zero.
 */
operator fun Real.unaryMinus(): Real {
  return when (realTypeCase) {
    RATIONAL -> recompute { it.setRational(-rational) }
    IRRATIONAL -> recompute { it.setIrrational(-irrational) }
    INTEGER -> recompute { it.setInteger(-integer) }
    REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $this.")
  }
}

/**
 * Returns an absolute value of this [Real] (that is, a non-negative [Real]).
 *
 * [isNegative] is guaranteed to return false for the returned value.
 */
fun abs(real: Real): Real = if (real.isNegative()) -real else real

private fun Real.recompute(transform: (Real.Builder) -> Real.Builder): Real {
  return transform(newBuilderForType()).build()
}
