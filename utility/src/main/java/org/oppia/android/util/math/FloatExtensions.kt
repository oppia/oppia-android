package org.oppia.android.util.math

import kotlin.math.abs

/**
 * The error margin used for approximating [Float] and [Double] equality checking, that is, the
 * largest distance from any particular number before a new value will be considered unequal (i.e.
 * all values between a float and (float-interval, float+interval) will be considered equal to the
 * float).
 */
const val FLOAT_EQUALITY_INTERVAL = 1e-5

/**
 * Returns whether this float approximately equals another based on a consistent epsilon value
 * ([FLOAT_EQUALITY_INTERVAL]).
 */
fun Float.isApproximatelyEqualTo(other: Float): Boolean {
  return abs(this - other) < FLOAT_EQUALITY_INTERVAL
}

/** Returns whether this double approximately equals another based on a consistent epsilon value
 * ([FLOAT_EQUALITY_INTERVAL]).
 */
fun Double.isApproximatelyEqualTo(other: Double): Boolean {
  return abs(this - other) < FLOAT_EQUALITY_INTERVAL
}

/**
 * Returns a string representation of this [Double] that keeps the double in pure decimal and never
 * relies on scientific notation (unlike [Double.toString]).
 */
fun Double.toPlainString(): String = toBigDecimal().toPlainString()
