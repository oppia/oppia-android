package org.oppia.android.util.math

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET
import kotlin.math.pow

fun Real.isRational(): Boolean = realTypeCase == RATIONAL

fun Real.isInteger(): Boolean = realTypeCase == INTEGER

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

operator fun Real.plus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::plus, Fraction::plus, Fraction::plus, Double::plus, Double::plus,
    Double::plus, Int::plus, Int::plus, Int::add
  )
}

operator fun Real.minus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::minus, Fraction::minus, Fraction::minus, Double::minus, Double::minus,
    Double::minus, Int::minus, Int::minus, Int::subtract
  )
}

operator fun Real.times(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::times, Fraction::times, Fraction::times, Double::times, Double::times,
    Double::times, Int::times, Int::times, Int::multiply
  )
}

operator fun Real.div(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::div, Fraction::div, Fraction::div, Double::div, Double::div, Double::div,
    Int::div, Int::div, Int::divide
  )
}

fun Real.pow(rhs: Real): Real {
  // Powers can really only be effectively done via floats or whole-number only fractions.
  return when (realTypeCase) {
    RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        RATIONAL -> recompute {
          if (rhs.rational.isOnlyWholeNumber()) {
            // The fraction can be retained.
            it.setRational(rational.pow(rhs.rational.wholeNumber))
          } else {
            // The fraction can't realistically be retained since it's being raised to an actual
            // fraction, resulting in an irrational number.
            it.setIrrational(rational.toDouble().pow(rhs.rational.toDouble()))
          }
        }
        IRRATIONAL -> recompute { it.setIrrational(rational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setRational(rational.pow(rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        RATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.rational)) }
        IRRATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setIrrational(irrational.pow(rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    INTEGER -> {
      // Left-hand side is an integer.
      when (rhs.realTypeCase) {
        RATIONAL -> {
          if (rhs.rational.isOnlyWholeNumber()) {
            // Whole number-only fractions are effectively just int^int.
            integer.pow(rhs.rational.wholeNumber)
          } else {
            // Otherwise, raising by a fraction will result in an irrational number.
            recompute { it.setIrrational(integer.toDouble().pow(rhs.rational.toDouble())) }
          }
        }
        IRRATIONAL -> recompute { it.setIrrational(integer.toDouble().pow(rhs.irrational)) }
        INTEGER -> integer.pow(rhs.integer)
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $this.")
  }
}

fun sqrt(real: Real): Real {
  return when (real.realTypeCase) {
    RATIONAL -> sqrt(real.rational)
    IRRATIONAL -> real.recompute { it.setIrrational(kotlin.math.sqrt(real.irrational)) }
    INTEGER -> sqrt(real.integer)
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $real.")
  }
}

fun abs(real: Real): Real = if (real.isNegative()) -real else real

private operator fun Double.plus(rhs: Fraction): Double = this + rhs.toDouble()
private operator fun Fraction.plus(rhs: Double): Double = toDouble() + rhs
private operator fun Fraction.plus(rhs: Int): Fraction = this + rhs.toWholeNumberFraction()
private operator fun Int.plus(rhs: Fraction): Fraction = toWholeNumberFraction() + rhs
private operator fun Double.minus(rhs: Fraction): Double = this - rhs.toDouble()
private operator fun Fraction.minus(rhs: Double): Double = toDouble() - rhs
private operator fun Fraction.minus(rhs: Int): Fraction = this - rhs.toWholeNumberFraction()
private operator fun Int.minus(rhs: Fraction): Fraction = toWholeNumberFraction() - rhs
private operator fun Double.times(rhs: Fraction): Double = this * rhs.toDouble()
private operator fun Fraction.times(rhs: Double): Double = toDouble() * rhs
private operator fun Fraction.times(rhs: Int): Fraction = this * rhs.toWholeNumberFraction()
private operator fun Int.times(rhs: Fraction): Fraction = toWholeNumberFraction() * rhs
private operator fun Double.div(rhs: Fraction): Double = this / rhs.toDouble()
private operator fun Fraction.div(rhs: Double): Double = toDouble() / rhs
private operator fun Fraction.div(rhs: Int): Fraction = this / rhs.toWholeNumberFraction()
private operator fun Int.div(rhs: Fraction): Fraction = toWholeNumberFraction() / rhs

private fun Int.add(rhs: Int): Real = Real.newBuilder().apply { integer = this@add + rhs }.build()
private fun Int.subtract(rhs: Int): Real = Real.newBuilder().apply {
  integer = this@subtract - rhs
}.build()
private fun Int.multiply(rhs: Int): Real = Real.newBuilder().apply {
  integer = this@multiply * rhs
}.build()
private fun Int.divide(rhs: Int): Real = Real.newBuilder().apply {
  // If rhs divides this integer, retain the integer.
  val lhs = this@divide
  if ((lhs % rhs) == 0) {
    integer = lhs / rhs
  } else {
    // Otherwise, keep precision by turning the division into a fraction.
    rational = Fraction.newBuilder().apply {
      isNegative = (lhs < 0) xor (rhs < 0)
      numerator = kotlin.math.abs(lhs)
      denominator = kotlin.math.abs(rhs)
    }.build()
  }
}.build()

private fun Double.pow(rhs: Fraction): Double = this.pow(rhs.toDouble())
private fun Fraction.pow(rhs: Double): Double = toDouble().pow(rhs)

private fun Int.pow(exp: Int): Real {
  return when {
    exp == 0 -> Real.newBuilder().apply { integer = 0 }.build()
    exp == 1 -> Real.newBuilder().apply { integer = this@pow }.build()
    exp < 0 -> Real.newBuilder().apply { rational = toWholeNumberFraction().pow(exp) }.build()
    else -> {
      // exp > 1
      var computed = this
      for (i in 0 until exp - 1) computed *= this
      Real.newBuilder().apply { integer = computed }.build()
    }
  }
}

private fun sqrt(fraction: Fraction): Real {
  val improper = fraction.toImproperForm()

  // Attempt to take the root of the fraction's numerator & denominator.
  val numeratorRoot = sqrt(improper.numerator)
  val denominatorRoot = sqrt(improper.denominator)

  // If both values stayed as integers, the original fraction can be retained. Otherwise, the
  // fraction must be evaluated by performing a division.
  return Real.newBuilder().apply {
    if (numeratorRoot.realTypeCase == denominatorRoot.realTypeCase && numeratorRoot.isInteger()) {
      val rootedFraction = Fraction.newBuilder().apply {
        isNegative = improper.isNegative
        numerator = numeratorRoot.integer
        denominator = denominatorRoot.integer
      }.build().toProperForm()
      if (rootedFraction.isOnlyWholeNumber()) {
        // If the fractional form doesn't need to be kept, remove it.
        integer = rootedFraction.toWholeNumber()
      } else {
        rational = rootedFraction
      }
    } else {
      irrational = numeratorRoot.toDouble()
    }
  }.build()
}

private fun sqrt(int: Int): Real {
  // First, check if the integer is a square. Reference for possible methods:
  // https://www.researchgate.net/post/How-to-decide-if-a-given-number-will-have-integer-square-root-or-not.
  var potentialRoot = 2
  while ((potentialRoot * potentialRoot) < int) {
    potentialRoot++
  }
  if (potentialRoot * potentialRoot == int) {
    // There's an exact integer representation of the root.
    return Real.newBuilder().apply {
      integer = potentialRoot
    }.build()
  }

  // Otherwise, compute the irrational square root.
  return Real.newBuilder().apply {
    irrational = kotlin.math.sqrt(int.toDouble())
  }.build()
}

private fun Real.recompute(transform: (Real.Builder) -> Real.Builder): Real {
  return transform(newBuilderForType()).build()
}

// TODO: consider replacing this with inline alternatives since they'll probably be simpler.
private fun combine(
  lhs: Real,
  rhs: Real,
  leftRationalRightRationalOp: (Fraction, Fraction) -> Fraction,
  leftRationalRightIrrationalOp: (Fraction, Double) -> Double,
  leftRationalRightIntegerOp: (Fraction, Int) -> Fraction,
  leftIrrationalRightRationalOp: (Double, Fraction) -> Double,
  leftIrrationalRightIrrationalOp: (Double, Double) -> Double,
  leftIrrationalRightIntegerOp: (Double, Int) -> Double,
  leftIntegerRightRationalOp: (Int, Fraction) -> Fraction,
  leftIntegerRightIrrationalOp: (Int, Double) -> Double,
  leftIntegerRightIntegerOp: (Int, Int) -> Real,
): Real {
  return when (lhs.realTypeCase) {
    RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute { it.setRational(leftRationalRightRationalOp(lhs.rational, rhs.rational)) }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftRationalRightIrrationalOp(lhs.rational, rhs.irrational))
          }
        INTEGER ->
          lhs.recompute { it.setRational(leftRationalRightIntegerOp(lhs.rational, rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightRationalOp(lhs.irrational, rhs.rational))
          }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightIrrationalOp(lhs.irrational, rhs.irrational))
          }
        INTEGER ->
          lhs.recompute {
            it.setIrrational(leftIrrationalRightIntegerOp(lhs.irrational, rhs.integer))
          }
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    INTEGER -> {
      // Left-hand side is an integer.
      when (rhs.realTypeCase) {
        RATIONAL ->
          lhs.recompute { it.setRational(leftIntegerRightRationalOp(lhs.integer, rhs.rational)) }
        IRRATIONAL ->
          lhs.recompute {
            it.setIrrational(leftIntegerRightIrrationalOp(lhs.integer, rhs.irrational))
          }
        INTEGER -> leftIntegerRightIntegerOp(lhs.integer, rhs.integer)
        REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw Exception("Invalid real: $lhs.")
  }
}
