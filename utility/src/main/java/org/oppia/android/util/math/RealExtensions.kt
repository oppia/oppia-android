package org.oppia.android.util.math

import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET

fun Real.isRational(): Boolean = realTypeCase == RATIONAL

fun Real.isNegative(): Boolean = when (realTypeCase) {
  RATIONAL -> rational.isNegative
  IRRATIONAL -> irrational < 0
  INTEGER -> integer < 0
  REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
}

fun Real.toDouble(): Double {
  return when (realTypeCase) {
    RATIONAL -> rational.toDouble()
    INTEGER -> integer.toDouble()
    IRRATIONAL -> irrational
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

fun Real.toPlainText(): String = when (realTypeCase) {
  // Note that the rational part is first converted to an improper fraction since mixed fractions
  // can't be expressed as a single coefficient in typical polynomial syntax).
  RATIONAL -> rational.toImproperForm().toAnswerString()
  IRRATIONAL -> irrational.toPlainString()
  INTEGER -> integer.toString()
  REALTYPE_NOT_SET, null -> ""
}

fun Real.isApproximatelyEqualTo(value: Double): Boolean {
  return toDouble().approximatelyEquals(value)
}

operator fun Real.unaryMinus(): Real {
  return when (realTypeCase) {
    RATIONAL -> recompute { it.setRational(-rational) }
    IRRATIONAL -> recompute { it.setIrrational(-irrational) }
    INTEGER -> recompute { it.setInteger(-integer) }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

fun abs(real: Real): Real = if (real.isNegative()) -real else real

private fun Real.recompute(transform: (Real.Builder) -> Real.Builder): Real {
  return transform(newBuilderForType()).build()
}
