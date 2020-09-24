package org.oppia.domain.util

import kotlin.math.abs

/** The error margin used for float equality by [Float.approximatelyEquals]. */
public const val FLOAT_EQUALITY_INTERVAL = 1e-5

/** Returns whether this float approximately equals another based on a consistent epsilon value. */
fun Float.approximatelyEquals(other: Float): Boolean {
  return abs(this - other) < FLOAT_EQUALITY_INTERVAL
}

/** Returns whether this double approximately equals another based on a consistent epsilon value. */
fun Double.approximatelyEquals(other: Double): Boolean {
  return abs(this - other) < FLOAT_EQUALITY_INTERVAL
}
