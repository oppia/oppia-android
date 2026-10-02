package org.oppia.android.domain.util

import org.oppia.android.app.model.NormalizedNumberWithUnits
import org.oppia.android.app.model.NormalizedUnit
import org.oppia.android.app.model.NormalizedUnit.BaseDimension
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberUnitExpression
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.NumberWithUnitsExpression
import org.oppia.android.util.math.NumberWithUnitsParser
import org.oppia.android.util.math.NumberWithUnitsParser.Companion.NumberWithUnitsParsingResult
import org.oppia.android.util.math.toDouble
import kotlin.math.pow

/**
 * Aggregates the units in a [NumberWithUnits] by summing the exponents of any duplicate units.
 * This is useful for normalizing [NumberWithUnits] instances for comparison, since the order of
 * units is irrelevant and duplicate units are effectively the same as a single unit with the
 * exponent equal to the sum of the exponents of the duplicate units.
 */
fun NumberWithUnits.aggregate(): NumberWithUnits {
  val aggregated = unitList.groupBy { it.unit }
    .map { (unit, units) ->
      NumberUnit.newBuilder()
        .setUnit(unit)
        .setExponent(units.sumOf { it.exponent })
        .build()
    }

  return toBuilder()
    .clearUnit()
    .addAllUnit(aggregated)
    .build()
}

/** Converts this value to base units and combines equivalent dimensions. */
fun NumberWithUnits.normalize(): NormalizedNumberWithUnits? {
  var value = when (numberTypeCase) {
    NumberWithUnits.NumberTypeCase.REAL -> real
    NumberWithUnits.NumberTypeCase.FRACTION -> fraction.toDouble()
    else -> return null
  }
  val dimensions = mutableMapOf<BaseDimension, Int>()
  var convertedValue = value
  unitList.forEach { unit ->
    val parsedUnit = when (val result = NumberWithUnitsParser.parseUnit(unit.unit)) {
      is NumberWithUnitsParsingResult.Success -> result.result
      is NumberWithUnitsParsingResult.Failure -> return null
    }
    val conversion = parsedUnit.toBaseUnit() ?: return null
    if (parsedUnit.unit == NumberUnitExpression.Unit.CELSIUS && unit.exponent == 1) {
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
  val builder = NumberWithUnits.newBuilder()

  when (numberTypeCase) {
    NumberWithUnitsExpression.NumberTypeCase.REAL -> builder.real = real
    NumberWithUnitsExpression.NumberTypeCase.FRACTION -> builder.fraction = fraction
    else -> {}
  }

  val unitsList = mutableListOf<NumberUnitExpression>()
  when (expressionFormatCase) {
    NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_EXPRESSION -> {
      unitsList.addAll(prefixValueExpression.prefixUnitsList)
    }
    NumberWithUnitsExpression.ExpressionFormatCase.VALUE_SUFFIX_EXPRESSION -> {
      unitsList.addAll(valueSuffixExpression.suffixUnitsList)
    }
    NumberWithUnitsExpression.ExpressionFormatCase.PREFIX_VALUE_SUFFIX_EXPRESSION -> {
      unitsList.addAll(prefixValueSuffixExpression.prefixUnitsList)
      unitsList.addAll(prefixValueSuffixExpression.suffixUnitsList)
    }
    else -> {}
  }

  unitsList.forEach { expr ->
    builder.addUnit(
      NumberUnit.newBuilder().apply {
        unit = expr.toUnitString()
        exponent = expr.exponent
      }
    )
  }

  return builder.build()
}

/** Converts a [NumberUnitExpression] into its canonical string representation. */
fun NumberUnitExpression.toUnitString(): String {
  val prefixStr = when (siPrefix) {
    NumberUnitExpression.SiPrefix.DECA -> "da"
    NumberUnitExpression.SiPrefix.HECTO -> "h"
    NumberUnitExpression.SiPrefix.KILO -> "k"
    NumberUnitExpression.SiPrefix.MEGA -> "M"
    NumberUnitExpression.SiPrefix.GIGA -> "G"
    NumberUnitExpression.SiPrefix.TERA -> "T"
    NumberUnitExpression.SiPrefix.PETA -> "P"
    NumberUnitExpression.SiPrefix.EXA -> "E"
    NumberUnitExpression.SiPrefix.ZETTA -> "Z"
    NumberUnitExpression.SiPrefix.YOTTA -> "Y"
    NumberUnitExpression.SiPrefix.DECI -> "d"
    NumberUnitExpression.SiPrefix.CENTI -> "c"
    NumberUnitExpression.SiPrefix.MILLI -> "m"
    NumberUnitExpression.SiPrefix.MICRO -> "u"
    NumberUnitExpression.SiPrefix.NANO -> "n"
    NumberUnitExpression.SiPrefix.PICO -> "p"
    NumberUnitExpression.SiPrefix.FEMTO -> "f"
    NumberUnitExpression.SiPrefix.ATTO -> "a"
    NumberUnitExpression.SiPrefix.ZEPTO -> "z"
    NumberUnitExpression.SiPrefix.YOCTO -> "y"
    else -> ""
  }
  val baseUnitStr = when (unit) {
    NumberUnitExpression.Unit.METER -> "m"
    NumberUnitExpression.Unit.INCH -> "in"
    NumberUnitExpression.Unit.FOOT -> "ft"
    NumberUnitExpression.Unit.YARD -> "yd"
    NumberUnitExpression.Unit.GRAM -> "g"
    NumberUnitExpression.Unit.GRAIN -> "gr"
    NumberUnitExpression.Unit.OUNCE -> "oz"
    NumberUnitExpression.Unit.SQUARE_METER -> "m2"
    NumberUnitExpression.Unit.SQUARE_INCH -> "sqin"
    NumberUnitExpression.Unit.SQUARE_FOOT -> "sqft"
    NumberUnitExpression.Unit.SQUARE_YARD -> "sqyd"
    NumberUnitExpression.Unit.CUBIC_METER -> "m3"
    NumberUnitExpression.Unit.LITER -> "L"
    NumberUnitExpression.Unit.CUBIC_CENTIMETER -> "cc"
    NumberUnitExpression.Unit.CUBIC_INCH -> "cuin"
    NumberUnitExpression.Unit.CUBIC_FOOT -> "cuft"
    NumberUnitExpression.Unit.CUBIC_YARD -> "cuyd"
    NumberUnitExpression.Unit.KELVIN -> "K"
    NumberUnitExpression.Unit.CELSIUS -> "degC"
    NumberUnitExpression.Unit.RADIAN -> "rad"
    NumberUnitExpression.Unit.DEGREE -> "deg"
    NumberUnitExpression.Unit.SECOND -> "s"
    NumberUnitExpression.Unit.MINUTE -> "min"
    NumberUnitExpression.Unit.HOUR -> "h"
    NumberUnitExpression.Unit.HERTZ -> "Hz"
    NumberUnitExpression.Unit.MOLE -> "mol"
    NumberUnitExpression.Unit.CANDELA -> "cd"
    NumberUnitExpression.Unit.NEWTON -> "N"
    NumberUnitExpression.Unit.JOULE -> "J"
    NumberUnitExpression.Unit.WATT -> "W"
    NumberUnitExpression.Unit.PASCAL -> "Pa"
    NumberUnitExpression.Unit.AMPERE -> "A"
    NumberUnitExpression.Unit.VOLT -> "V"
    NumberUnitExpression.Unit.OHM -> "ohm"
    NumberUnitExpression.Unit.DOLLAR -> "$"
    NumberUnitExpression.Unit.CENT -> "¢"
    NumberUnitExpression.Unit.RUPEE -> "Rs"
    NumberUnitExpression.Unit.PAISA -> "paisa"
    else -> ""
  }
  return prefixStr + baseUnitStr
}

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
    NumberUnitExpression.Unit.DOLLAR -> createConversion(100.0, BaseDimension.CURRENCY_DOLLAR to 1)
    NumberUnitExpression.Unit.CENT -> createConversion(0.01, BaseDimension.CURRENCY_DOLLAR to 1)
    NumberUnitExpression.Unit.RUPEE -> createConversion(1.0, BaseDimension.CURRENCY_RUPEE to 1)
    NumberUnitExpression.Unit.PAISA -> createConversion(0.01, BaseDimension.CURRENCY_RUPEE to 1)
    else -> return null
  }
  val prefixFactor = when (siPrefix) {
    NumberUnitExpression.SiPrefix.KILO -> 1e3
    NumberUnitExpression.SiPrefix.CENTI -> 1e-2
    NumberUnitExpression.SiPrefix.MILLI -> 1e-3
    NumberUnitExpression.SiPrefix.MICRO -> 1e-6
    NumberUnitExpression.SiPrefix.MEGA -> 1e6
    NumberUnitExpression.SiPrefix.HECTO -> 1e2
    NumberUnitExpression.SiPrefix.DECA -> 1e1
    NumberUnitExpression.SiPrefix.DECI -> 1e-1
    NumberUnitExpression.SiPrefix.GIGA -> 1e9
    else -> 1.0
  }
  return if (prefixFactor == 1.0)
    base
  else
    base.toBuilder().setValue(base.value * prefixFactor).build()
}
