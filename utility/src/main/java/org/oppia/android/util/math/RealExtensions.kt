package org.oppia.android.util.math

import kotlin.math.absoluteValue
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Real
import org.oppia.android.app.model.Real.RealTypeCase.INTEGER
import org.oppia.android.app.model.Real.RealTypeCase.IRRATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.RATIONAL
import org.oppia.android.app.model.Real.RealTypeCase.REALTYPE_NOT_SET
import kotlin.math.pow

/**
 * Returns whether this [Real] is explicitly a rational type (i.e. a fraction).
 *
 * This returns false if the real is an integer despite that being mathematically rational.
 */
fun Real.isRational(): Boolean = realTypeCase == RATIONAL

/**
 * Returns whether this [Real] is explicitly an integer.
 *
 * This returns false if the real is a rational despite that being mathematically an integer (e.g. a
 * whole number fraction).
 */
fun Real.isInteger(): Boolean = realTypeCase == INTEGER

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
 * Adds this [Real] with another and returns the result.
 *
 * Neither [Real] being added are changed during the operation.
 *
 * Note that this function will always succeed (unless one of the [Real]s is malformed or
 * incomplete), but the type of [Real] that's returned depends on the constituent [Real]s being
 * added. For reference, here's how the conversion behaves:
 *
 * |---------------------------------------------------|
 * | +          | integer    | rational   | irrational |
 * |------------|------------|------------|------------|
 * | integer    | integer    | rational   | irrational |
 * | rational   | rational   | rational   | irrational |
 * | irrational | irrational | irrational | irrational |
 * |---------------------------------------------------|
 */
operator fun Real.plus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::plus, Fraction::plus, Fraction::plus, Double::plus, Double::plus,
    Double::plus, Int::plus, Int::plus, Int::add
  )
}

/**
 * Subtracts this [Real] from another and returns the result.
 *
 * Neither [Real] being subtracted are changed during the operation.
 *
 * Note that this function will always succeed (unless one of the [Real]s is malformed or
 * incomplete), but the type of [Real] that's returned depends on the constituent [Real]s being
 * subtracted. For reference, see [Real.plus] (the same type conversion is used).
 */
operator fun Real.minus(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::minus, Fraction::minus, Fraction::minus, Double::minus, Double::minus,
    Double::minus, Int::minus, Int::minus, Int::subtract
  )
}

/**
 * Multiplies this [Real] with another and returns the result.
 *
 * Neither [Real] being multiplied are changed during the operation.
 *
 * Note that this function will always succeed (unless one of the [Real]s is malformed or
 * incomplete), but the type of [Real] that's returned depends on the constituent [Real]s being
 * multiplied. For reference, see [Real.plus] (the same type conversion is used).
 *
 * Note that effective divisions by zero (i.e. fractions with zero denominators) may result in
 * either an infinity being returned or an exception being thrown.
 */
operator fun Real.times(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::times, Fraction::times, Fraction::times, Double::times, Double::times,
    Double::times, Int::times, Int::times, Int::multiply
  )
}

/**
 * Divides this [Real] by another and returns the result.
 *
 * Neither [Real] being divided are changed during the operation.
 *
 * Note that this function will always succeed (unless one of the [Real]s is malformed or
 * incomplete), but the type of [Real] that's returned depends on the constituent [Real]s being
 * divided. For reference, see [Real.plus] (the same type conversion is used).
 *
 * Note also that divisions by zero may result in either an exception being thrown, or an infinity
 * being returned.
 */
operator fun Real.div(rhs: Real): Real {
  return combine(
    this, rhs, Fraction::div, Fraction::div, Fraction::div, Double::div, Double::div, Double::div,
    Int::div, Int::div, Int::divide
  )
}

/**
 * Computes the power of this [Real] raised to [rhs] and returns the result.
 *
 * Neither [Real] being combined are changed during the operation.
 *
 * As this is an infix function, it should be called as so (example):
 * ```kotlin
 * val result = baseReal pow powerReal
 * ```
 *
 * This function can fail in a few circumstances:
 * - One of the [Real]s is malformed or incomplete (such as a default instance).
 * - In cases where a root is being taken (i.e. when |[rhs]| < 1), if the root cannot be taken
 *   either an exception will be thrown or NaN will be returned (such as trying to take the even
 *   root of a negative value).
 *
 * Further, note that this function represents the real value root rather than the principal root,
 * so negative bases are allowed so long as the root being used is odd. For non-integerlike powers,
 * the base should never be negative except for fractions that could result in a positive base after
 * exponentiation.
 *
 * This function special cases 0^0 to return 1 in all cases for consistency with the system ``pow``
 * function and other languages, per: https://stackoverflow.com/a/19955996.
 *
 * Finally, this function also attempts to retain maximum precision in much the same way as [sqrt]
 * and [Real.plus] except there are more cases when a value may change types. See the following
 * table for reference:
 *
 * |----------------------------------------------------------------------------------------------|
 * | pow        | positive int | negative int | rootable rational* | other rationals | irrational |
 * |------------|--------------|--------------|--------------------|-----------------|------------|
 * | integer    | integer      | rational     | rational           | irrational      | irrational |
 * | rational   | rational     | rational     | rational           | irrational      | irrational |
 * | irrational | irrational   | irrational   | irrational         | irrational      | irrational |
 * |----------------------------------------------------------------------------------------------|
 *
 * *This corresponds to fraction powers whose denominator (which are treated as roots) can perform a
 * perfect square root of either the integer base (for integer [Real]s) or both the numerator and
 * denominator integers (for rational [Real]s).
 *
 * (Note that the left column represents the left-hand side and the top row represents the
 * right-hand side of the operation).
 */
infix fun Real.pow(rhs: Real): Real {
  // Powers can really only be effectively done via floats or whole-number only fractions.
  return when (realTypeCase) {
    RATIONAL -> {
      // Left-hand side is Fraction.
      when (rhs.realTypeCase) {
        // Anything raised by a fraction is pow'd by the numerator and rooted by the denominator.
        RATIONAL -> rhs.rational.toImproperForm().let { power ->
          (rational pow power.numerator).root(power.denominator, power.isNegative)
        }
        IRRATIONAL -> recompute { it.setIrrational(rational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setRational(rational pow rhs.integer) }
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
      }
    }
    IRRATIONAL -> {
      // Left-hand side is a double.
      when (rhs.realTypeCase) {
        RATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.rational)) }
        IRRATIONAL -> recompute { it.setIrrational(irrational.pow(rhs.irrational)) }
        INTEGER -> recompute { it.setIrrational(irrational.pow(rhs.integer)) }
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
      }
    }
    INTEGER -> {
      // Left-hand side is an integer.
      when (rhs.realTypeCase) {
        // An integer raised to a fraction can use the same approach as above (fraction raised to
        // fraction) by treating the integer as a whole number fraction.
        RATIONAL -> rhs.rational.toImproperForm().let { power ->
          (integer.toWholeNumberFraction() pow power.numerator).root(
            power.denominator, power.isNegative
          )
        }
        IRRATIONAL -> recompute { it.setIrrational(integer.toDouble().pow(rhs.irrational)) }
        INTEGER -> integer.pow(rhs.integer)
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $this.")
  }
}

/**
 * Returns the square root of the specified [Real].
 *
 * [real] is not changed as a result of this operation (a new [Real] value is returned).
 *
 * Failure cases:
 * - An invalid [Real] is passed in (such as a default instance), resulting in an exception being
 *   thrown.
 * - A negative value is passed in (this will either result in an exception or a NaN being
 *   returned).
 *
 * Similar to [Real.plus] & other operations, this function attempts to retain as much precision as
 * possible by first performing perfect roots before needing to perform a numerical approximation.
 * This is achieved by attempting to take perfect integer roots for integer and rational types and,
 * if that's not possible, then converting to a double. See the following conversion table for
 * reference:
 *
 * |------------------------------------------------|
 * | sqrt       | perfect square | all other values |
 * |------------|----------------|------------------|
 * | integer    | integer        | irrational       |
 * | rational   | rational       | irrational       |
 * | irrational | irrational     | irrational       |
 * |------------------------------------------------|
 */
fun sqrt(real: Real): Real {
  return when (real.realTypeCase) {
    RATIONAL -> sqrt(real.rational)
    IRRATIONAL -> real.recompute { it.setIrrational(kotlin.math.sqrt(real.irrational)) }
    INTEGER -> sqrt(real.integer)
    REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $real.")
  }
}

/**
 * Returns an absolute value of this [Real] (that is, a non-negative [Real]).
 *
 * [isNegative] is guaranteed to return false for the returned value.
 */
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
    }.build().toProperForm()
  }
}.build()

private fun Double.pow(rhs: Fraction): Double = this.pow(rhs.toDouble())
private fun Fraction.pow(rhs: Double): Double = toDouble().pow(rhs)

private fun Int.pow(exp: Int): Real {
  return when {
    exp == 0 -> Real.newBuilder().apply { integer = 1 }.build()
    exp == 1 -> Real.newBuilder().apply { integer = this@pow }.build()
    exp < 0 -> Real.newBuilder().apply { rational = toWholeNumberFraction() pow exp }.build()
    else -> {
      // exp > 1
      var computed = this
      for (i in 0 until exp - 1) computed *= this
      Real.newBuilder().apply { integer = computed }.build()
    }
  }
}

private fun sqrt(fraction: Fraction): Real = fraction.root(base = 2, invert = false)

private fun Fraction.root(base: Int, invert: Boolean): Real {
  check(base > 0) { "Expected base of 1 or higher, not: $base" }

  val adjustedFraction = toImproperForm()
  val adjustedNum =
    if (adjustedFraction.isNegative) -adjustedFraction.numerator else adjustedFraction.numerator
  val adjustedDenom = adjustedFraction.denominator
  val rootedNumerator = if (invert) root(adjustedDenom, base) else root(adjustedNum, base)
  val rootedDenominator = if (invert) root(adjustedNum, base) else root(adjustedDenom, base)
  return if (rootedNumerator.isInteger() && rootedDenominator.isInteger()) {
    Real.newBuilder().apply {
      rational = Fraction.newBuilder().apply {
        isNegative = rootedNumerator.isNegative() || rootedDenominator.isNegative()
        numerator = rootedNumerator.integer.absoluteValue
        denominator = rootedDenominator.integer.absoluteValue
      }.build().toProperForm()
    }.build()
  } else {
    // One or both of the components of the fraction can't be rooted, so compute an irrational
    // version.
    Real.newBuilder().apply {
      irrational = rootedNumerator.toDouble() / rootedDenominator.toDouble()
    }.build()
  }
}

private fun sqrt(int: Int): Real = root(int, base = 2)

private fun root(int: Int, base: Int): Real {
  // First, check if the integer is a root. Base reference for possible methods:
  // https://www.researchgate.net/post/How-to-decide-if-a-given-number-will-have-integer-square-root-or-not.
  if (int == 0 && base == 0) {
    // This is considered a conventional identity per https://stackoverflow.com/a/19955996 that
    // doesn't match mathematics definitions (but it does bring parity with the system's pow()
    // function).
    return Real.newBuilder().apply {
      integer = 1
    }.build()
  }

  check(base > 0) { "Expected base of 1 or higher, not: $base" }
  check((int < 0 && base.isOdd()) || int >= 0) { "Radicand results in imaginary number: $int" }

  when {
    int == 0 -> {
      // 0^x is always zero.
      return Real.newBuilder().apply {
        integer = 0
      }.build()
    }
    int == 1 || int == 0 || base == 0 -> {
      // 1^x and x^0 are always 1.
      return Real.newBuilder().apply {
        integer = 1
      }.build()
    }
    base == 1 -> {
      // x^1 is always x.
      return Real.newBuilder().apply {
        integer = int
      }.build()
    }
  }

  val radicand = int.absoluteValue
  var potentialRoot = base
  while (potentialRoot.pow(base).integer < radicand) {
    potentialRoot++
  }
  if (potentialRoot.pow(base).integer == radicand) {
    // There's an exact integer representation of the root.
    if (int < 0 && base.isOdd()) {
      // Odd roots of negative numbers retain the negative.
      potentialRoot = -potentialRoot
    }
    return Real.newBuilder().apply {
      integer = potentialRoot
    }.build()
  }

  // Otherwise, compute the irrational square root.
  return Real.newBuilder().apply {
    irrational = if (base == 2) {
      kotlin.math.sqrt(int.toDouble())
    } else int.toDouble().pow(1.0 / base.toDouble())
  }.build()
}

private fun Int.isOdd() = this % 2 == 1

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
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
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
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
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
        REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $rhs.")
      }
    }
    REALTYPE_NOT_SET, null -> throw IllegalStateException("Invalid real: $lhs.")
  }
}
