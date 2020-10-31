package org.oppia.android.domain.classify

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.StringList

/**
 * Helper class for test cases which can provide the [InteractionObject]
 * of a particular ObjectTypeCase.
 */
object InteractionObjectTestBuilder {

  fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  fun createListOfSetsOfHtmlStrings(listOfStringList: List<StringList>): InteractionObject {
    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(
        listOfStringList
      )
      .build()

    return InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()
  }

  fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }

  fun createWholeNumber(isNegative: Boolean, value: Int): InteractionObject {
    // Whole number fractions imply '0/1' fractional parts.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(value)
        .setNumerator(0)
        .setDenominator(1)
        .build()
    ).build()
  }

  fun createFraction(
    isNegative: Boolean,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    // Fraction-only numbers imply no whole number.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  fun createMixedNumber(
    isNegative: Boolean,
    wholeNumber: Int,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(wholeNumber)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  /** Creates fraction part for [NumberWithUnits].
   */
  fun createNumberWithUnitsForFraction(number: Fraction, units: List<NumberUnit>):
    InteractionObject {
      val numberWithUnits = NumberWithUnits.newBuilder()
        .addAllUnit(units)
        .setFraction(number)
        .build()

      return InteractionObject.newBuilder().setNumberWithUnits(numberWithUnits).build()
    }

  /** Creates real part for [NumberWithUnits].
   */
  fun createNumberWithUnitsForReal(number: Double, units: List<NumberUnit>):
    InteractionObject {
      val numberWithUnits = NumberWithUnits.newBuilder()
        .addAllUnit(units)
        .setReal(number)
        .build()

      return InteractionObject.newBuilder().setNumberWithUnits(numberWithUnits).build()
    }

  /** Creates [NumberUnit] using the [unit] and [exponent] for [NumberWithUnits].
   */
  fun createNumberUnit(unit: String, exponent: Int): NumberUnit {
    return NumberUnit.newBuilder().setUnit(unit).setExponent(exponent).build()
  }

  fun createSignedInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setSignedInt(value).build()
  }

  fun createReal(value: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(value).build()
  }

  fun createInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setReal(value.toDouble()).build()
  }

  fun createRatio(value: List<Int>): InteractionObject {
    return InteractionObject.newBuilder().setRatioExpression(
      RatioExpression.newBuilder().addAllRatioComponent(value)
    ).build()
  }
}
