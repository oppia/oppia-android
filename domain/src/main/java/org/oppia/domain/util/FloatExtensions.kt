package org.oppia.domain.util

import kotlin.math.abs

public const val EPSILON = 1e-5

/** Returns whether this float approximately equals another based on a consistent epsilon value. */
fun Float.approximatelyEquals(other: Float): Boolean {
  return abs(this - other) < EPSILON
}

/** Returns whether this double approximately equals another based on a consistent epsilon value. */
fun Double.approximatelyEquals(other: Double): Boolean {
  return abs(this - other) < EPSILON
}
