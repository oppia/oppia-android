package org.oppia.android.util.math

import kotlin.math.abs

/**
 * The error margin used for approximately [Float] equality checking, that is, the largest distance
 * from any particular number before a new value will be considered unequal (i.e. all values between
 * a float and (float-interval, float+interval) will be considered equal to the float).
 *
 * Note that the machine epsilon value from https://en.wikipedia.org/wiki/Machine_epsilon is defined
 * defined as the smallest value that, when added to, or subtract from, 1, will result in a value
 * that is exactly equal to 1. A slightly larger value is picked here for some allowance in
 * variance.
 */
const val FLOAT_EQUALITY_EPSILON: Float = 1e-6f

/**
 * The error margin used for approximately [Double] equality checking.
 *
 * See [FLOAT_EQUALITY_EPSILON] for an explanation of this value.
 */
const val DOUBLE_EQUALITY_EPSILON: Double = 1e-15

/**
 * Returns whether this float approximately equals another based on a consistent epsilon value
 * ([FLOAT_EQUALITY_EPSILON]).
 */
fun Float.approximatelyEquals(other: Float): Boolean {
  return abs(this - other) < FLOAT_EQUALITY_EPSILON
}

/** Returns whether this double approximately equals another based on a consistent epsilon value
 * ([DOUBLE_EQUALITY_EPSILON]).
 */
fun Double.approximatelyEquals(other: Double): Boolean {
  return abs(this - other) < DOUBLE_EQUALITY_EPSILON
}

/**
 * Returns a string representation of this [Double] that keeps the double in pure decimal and never
 * relies on scientific notation (unlike [Double.toString]).
 */
fun Double.toPlainString(): String = toBigDecimal().toPlainString()
