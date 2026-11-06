package org.oppia.android.domain.util

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.NormalizedNumberWithUnits
import org.oppia.android.app.model.NormalizedUnit
import org.oppia.android.app.model.NormalizedUnit.BaseDimension
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberUnitExpression
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.toUnitString
import org.oppia.android.util.math.toDouble
import java.math.BigDecimal
import kotlin.math.pow

/** Unit strings that are written before the numeric value (e.g. "$5" or "Rs 5"). */
private val PREFIX_UNIT_STRINGS = setOf("$", "Rs", "₹")

/**
 * Returns a raw expression representing [this] value that can be parsed by
 * [org.oppia.android.util.math.NumberWithUnitsParser], e.g. "$ 5", "-3/2 kg m^-2", or "2.5 m s^-1".
 *
 * Fractions are always represented in improper form (since the parser doesn't support mixed
 * numbers), and duplicate units are retained.
 */
fun NumberWithUnits.toRawExpression(): String {
  val valueString = when (numberTypeCase) {
    NumberWithUnits.NumberTypeCase.REAL -> real.toRawExpression()
    NumberWithUnits.NumberTypeCase.FRACTION -> fraction.toRawImproperFraction()
    else -> ""
  }
  val (prefixUnits, suffixUnits) =
    unitList.partition { it.unit in PREFIX_UNIT_STRINGS && it.exponent == 1 }
  return (prefixUnits.map { it.unit } + valueString + suffixUnits.map { it.toRawExpression() })
    .filter { it.isNotEmpty() }
    .joinToString(separator = " ")
}

/** Returns all units of [this] expression, regardless of whether they precede the value. */
fun NumberWithUnitsExpression.getAllUnits(): List<NumberUnitExpression> {
  return when (expressionFormatCase) {
    NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_EXPRESSION ->
      prefixValueExpression.prefixUnitsList
    NumberWithUnitsExpression.ExpressionFormatCase.VALUE_SUFFIX_EXPRESSION ->
      valueSuffixExpression.suffixUnitsList
    NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_SUFFIX_EXPRESSION ->
      prefixValueSuffixExpression.prefixUnitsList + prefixValueSuffixExpression.suffixUnitsList
    else -> listOf()
  }
}

/**
 * Converts this value to base units and combines equivalent dimensions, or returns null if it
 * has no value or contains an unsupported unit.
 */
fun NumberWithUnitsExpression.normalize(): NormalizedNumberWithUnits? {
  var convertedValue = when (numberTypeCase) {
    NumberWithUnitsExpression.NumberTypeCase.REAL -> real
    NumberWithUnitsExpression.NumberTypeCase.FRACTION -> fraction.toDouble()
    else -> return null
  }
  val dimensions = mutableMapOf<BaseDimension, Int>()
  getAllUnits().forEach { unit ->
    val conversion = unit.toBaseUnit() ?: return null
    if (unit.unit == NumberUnitExpression.Unit.CELSIUS && unit.exponent == 1) {
      convertedValue += 273.15 // Offset for Celsius to Kelvin.
    }
    convertedValue *= conversion.value.pow(unit.exponent)
    conversion.unitList.forEach { normalizedUnit ->
      dimensions[normalizedUnit.dimension] =
        (dimensions[normalizedUnit.dimension] ?: 0) + normalizedUnit.exponent * unit.exponent
    }
  }
  return NormalizedNumberWithUnits.newBuilder().apply {
    value = convertedValue
    addAllUnit(
      // Sort by enum ordinal for deterministic ordering.
      dimensions.filterValues { it != 0 }
        .toSortedMap(compareBy { it.ordinal })
        .map { (dimension, exponent) ->
          NormalizedUnit
            .newBuilder()
            .setDimension(dimension)
            .setExponent(exponent)
            .build()
        }
    )
  }.build()
}

/**
 * Converts a [NumberWithUnitsExpression] produced by the parser into a [NumberWithUnits] suitable
 * for use in an InteractionObject.
 */
fun NumberWithUnitsExpression.toNumberWithUnits(): NumberWithUnits {
  val expression = this
  return NumberWithUnits.newBuilder().apply {
    when (expression.numberTypeCase) {
      NumberWithUnitsExpression.NumberTypeCase.REAL -> real = expression.real
      NumberWithUnitsExpression.NumberTypeCase.FRACTION -> fraction = expression.fraction
      else -> {}
    }
    expression.getAllUnits().forEach { unitExpression ->
      addUnit(
        NumberUnit.newBuilder().apply {
          unit = unitExpression.toUnitString()
          exponent = unitExpression.exponent
        }
      )
    }
  }.build()
}

private fun Double.toRawExpression(): String {
  if (!isFinite()) return toString()
  // BigDecimal avoids scientific notation (e.g. "1.0E-7"), which the parser doesn't support. A
  // decimal point is always kept so that large values aren't tokenized as (overflowing) integers.
  val plainString = BigDecimal.valueOf(this).toPlainString()
  return if ('.' in plainString) plainString else "$plainString.0"
}

private fun Fraction.toRawImproperFraction(): String {
  val sign = if (isNegative) "-" else ""
  return "$sign${wholeNumber * denominator + numerator}/$denominator"
}

private fun NumberUnit.toRawExpression(): String =
  if (exponent == 1) unit else "$unit^$exponent"

private fun createConversion(
  factor: Double,
  vararg dimensions: Pair<BaseDimension, Int>
): NormalizedNumberWithUnits {
  return NormalizedNumberWithUnits.newBuilder()
    .setValue(factor)
    .addAllUnit(
      dimensions.map { (dim, exp) ->
        NormalizedUnit.newBuilder().setDimension(dim).setExponent(exp).build()
      }
    )
    .build()
}

private fun NumberUnitExpression.toBaseUnit(): NormalizedNumberWithUnits? {
  val base = when (unit) {
    NumberUnitExpression.Unit.METER -> createConversion(1.0, BaseDimension.LENGTH to 1)
    NumberUnitExpression.Unit.INCH -> createConversion(0.0254, BaseDimension.LENGTH to 1)
    NumberUnitExpression.Unit.FOOT -> createConversion(0.3048, BaseDimension.LENGTH to 1)
    NumberUnitExpression.Unit.YARD -> createConversion(0.9144, BaseDimension.LENGTH to 1)
    NumberUnitExpression.Unit.GRAM -> createConversion(1.0, BaseDimension.MASS to 1)
    NumberUnitExpression.Unit.GRAIN -> createConversion(0.0647989, BaseDimension.MASS to 1)
    NumberUnitExpression.Unit.OUNCE -> createConversion(28.3495, BaseDimension.MASS to 1)
    NumberUnitExpression.Unit.SQUARE_METER -> createConversion(1.0, BaseDimension.AREA to 2)
    NumberUnitExpression.Unit.SQUARE_INCH -> createConversion(0.00064516, BaseDimension.AREA to 2)
    NumberUnitExpression.Unit.SQUARE_FOOT -> createConversion(0.092903, BaseDimension.AREA to 2)
    NumberUnitExpression.Unit.SQUARE_YARD -> createConversion(0.836127, BaseDimension.AREA to 2)
    NumberUnitExpression.Unit.CUBIC_METER -> createConversion(1.0, BaseDimension.VOLUME to 3)
    NumberUnitExpression.Unit.LITER -> createConversion(0.001, BaseDimension.VOLUME to 3)
    NumberUnitExpression.Unit.CUBIC_CENTIMETER -> createConversion(1e-6, BaseDimension.VOLUME to 3)
    NumberUnitExpression.Unit.CUBIC_INCH -> createConversion(
      0.000016387,
      BaseDimension.VOLUME to 3
    )
    NumberUnitExpression.Unit.CUBIC_FOOT -> createConversion(0.0283168, BaseDimension.VOLUME to 3)
    NumberUnitExpression.Unit.CUBIC_YARD -> createConversion(0.764555, BaseDimension.VOLUME to 3)
    NumberUnitExpression.Unit.KELVIN -> createConversion(1.0, BaseDimension.TEMPERATURE to 1)
    NumberUnitExpression.Unit.CELSIUS -> createConversion(1.0, BaseDimension.TEMPERATURE to 1)
    NumberUnitExpression.Unit.RADIAN -> createConversion(1.0, BaseDimension.ANGLE to 1)
    NumberUnitExpression.Unit.DEGREE -> createConversion(Math.PI / 180, BaseDimension.ANGLE to 1)
    NumberUnitExpression.Unit.SECOND -> createConversion(1.0, BaseDimension.TIME to 1)
    NumberUnitExpression.Unit.MINUTE -> createConversion(60.0, BaseDimension.TIME to 1)
    NumberUnitExpression.Unit.HOUR -> createConversion(3600.0, BaseDimension.TIME to 1)
    NumberUnitExpression.Unit.HERTZ -> createConversion(1.0, BaseDimension.TIME to -1)
    NumberUnitExpression.Unit.MOLE -> createConversion(1.0, BaseDimension.AMOUNT to 1)
    NumberUnitExpression.Unit.CANDELA -> createConversion(
      1.0,
      BaseDimension.LUMINOUS_INTENSITY to 1
    )
    NumberUnitExpression.Unit.NEWTON -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to 1,
      BaseDimension.TIME to -2
    )
    NumberUnitExpression.Unit.JOULE -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to 2,
      BaseDimension.TIME to -2
    )
    NumberUnitExpression.Unit.WATT -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to 2,
      BaseDimension.TIME to -3
    )
    NumberUnitExpression.Unit.PASCAL -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to -1,
      BaseDimension.TIME to -2
    )
    NumberUnitExpression.Unit.AMPERE -> createConversion(1.0, BaseDimension.CURRENT to 1)
    NumberUnitExpression.Unit.VOLT -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to 2,
      BaseDimension.TIME to -3,
      BaseDimension.CURRENT to -1
    )
    NumberUnitExpression.Unit.OHM -> createConversion(
      1000.0,
      BaseDimension.MASS to 1,
      BaseDimension.LENGTH to 2,
      BaseDimension.TIME to -3,
      BaseDimension.CURRENT to -2
    )
    NumberUnitExpression.Unit.DOLLAR -> createConversion(1.0, BaseDimension.CURRENCY_DOLLAR to 1)
    NumberUnitExpression.Unit.CENT -> createConversion(0.01, BaseDimension.CURRENCY_DOLLAR to 1)
    NumberUnitExpression.Unit.RUPEE -> createConversion(1.0, BaseDimension.CURRENCY_RUPEE to 1)
    NumberUnitExpression.Unit.PAISA -> createConversion(0.01, BaseDimension.CURRENCY_RUPEE to 1)
    else -> return null
  }
  val prefixFactor = when (siPrefix) {
    NumberUnitExpression.SiPrefix.DECA -> 1e1
    NumberUnitExpression.SiPrefix.HECTO -> 1e2
    NumberUnitExpression.SiPrefix.KILO -> 1e3
    NumberUnitExpression.SiPrefix.MEGA -> 1e6
    NumberUnitExpression.SiPrefix.GIGA -> 1e9
    NumberUnitExpression.SiPrefix.TERA -> 1e12
    NumberUnitExpression.SiPrefix.PETA -> 1e15
    NumberUnitExpression.SiPrefix.EXA -> 1e18
    NumberUnitExpression.SiPrefix.ZETTA -> 1e21
    NumberUnitExpression.SiPrefix.YOTTA -> 1e24
    NumberUnitExpression.SiPrefix.DECI -> 1e-1
    NumberUnitExpression.SiPrefix.CENTI -> 1e-2
    NumberUnitExpression.SiPrefix.MILLI -> 1e-3
    NumberUnitExpression.SiPrefix.MICRO -> 1e-6
    NumberUnitExpression.SiPrefix.NANO -> 1e-9
    NumberUnitExpression.SiPrefix.PICO -> 1e-12
    NumberUnitExpression.SiPrefix.FEMTO -> 1e-15
    NumberUnitExpression.SiPrefix.ATTO -> 1e-18
    NumberUnitExpression.SiPrefix.ZEPTO -> 1e-21
    NumberUnitExpression.SiPrefix.YOCTO -> 1e-24
    else -> 1.0
  }
  return if (prefixFactor == 1.0)
    base
  else
    base.toBuilder().setValue(base.value * prefixFactor).build()
}
