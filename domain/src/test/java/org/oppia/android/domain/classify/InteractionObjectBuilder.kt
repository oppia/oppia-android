package org.oppia.android.domain.classify

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.StringList
import javax.inject.Inject

/** Helper class which can provide with the [InteractionObject] of a particular ObjectTypeCase. */
class InteractionObjectBuilder @Inject constructor() {

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

  fun createSignedInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setSignedInt(value).build()
  }

  fun createReal(value: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(value).build()
  }

  fun createInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setReal(value.toDouble()).build()
  }
}
