package org.oppia.domain.util

import org.oppia.app.model.ClickOnImage
import org.oppia.app.model.Fraction
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.InteractionObject.ObjectTypeCase.BOOL_VALUE
import org.oppia.app.model.InteractionObject.ObjectTypeCase.CLICK_ON_IMAGE
import org.oppia.app.model.InteractionObject.ObjectTypeCase.FRACTION
import org.oppia.app.model.InteractionObject.ObjectTypeCase.IMAGE_WITH_REGIONS
import org.oppia.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING
import org.oppia.app.model.InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT
import org.oppia.app.model.InteractionObject.ObjectTypeCase.NORMALIZED_STRING
import org.oppia.app.model.InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS
import org.oppia.app.model.InteractionObject.ObjectTypeCase.OBJECTTYPE_NOT_SET
import org.oppia.app.model.InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
import org.oppia.app.model.InteractionObject.ObjectTypeCase.REAL
import org.oppia.app.model.InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING
import org.oppia.app.model.InteractionObject.ObjectTypeCase.SIGNED_INT
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.RatioExpression
import org.oppia.app.model.StringList

/** Returns a parsable string representation of a user-submitted answer version of this [InteractionObject]. */
fun InteractionObject.toAnswerString(): String {
  return when (checkNotNull(objectTypeCase)) {
    NORMALIZED_STRING -> normalizedString
    SIGNED_INT -> signedInt.toString()
    NON_NEGATIVE_INT -> nonNegativeInt.toString()
    REAL -> real.toString()
    BOOL_VALUE -> boolValue.toString()
    NUMBER_WITH_UNITS -> numberWithUnits.toAnswerString()
    SET_OF_HTML_STRING -> setOfHtmlString.toAnswerString()
    FRACTION -> fraction.toAnswerString()
    LIST_OF_SETS_OF_HTML_STRING -> listOfSetsOfHtmlString.toAnswerString()
    IMAGE_WITH_REGIONS -> imageWithRegions.toAnswerString()
    CLICK_ON_IMAGE -> clickOnImage.toAnswerString()
    RATIO_EXPRESSION -> ratioExpression.toAnswerString()
    OBJECTTYPE_NOT_SET -> "" // The default InteractionObject should be an empty string.
  }
}

// https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/NumberWithUnitsObjectFactory.ts#L50
private fun NumberWithUnits.toAnswerString(): String {
  val prefixedUnits = unitList.filter(::isPrefixUnit)
  val suffixedUnits = unitList.filterNot(::isPrefixUnit)

  val prefixString = prefixedUnits.joinToString(separator = " ")
  val suffixedString =
    suffixedUnits.joinToString(separator = " ", transform = NumberUnit::toAnswerStringPart)
  val valueString = if (numberTypeCase == NumberWithUnits.NumberTypeCase.REAL) {
    real.toString()
  } else {
    fraction.toAnswerString()
  }

  return "$prefixString$valueString $suffixedString".trim()
}

// TODO(#152): Standardize these with a currency library.
private fun isPrefixUnit(numberUnit: NumberUnit): Boolean {
  return numberUnit.unit in listOf("$", "Rs", "₹", "€", "£", "¥")
}

// https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/UnitsObjectFactory.ts#L49
private fun NumberUnit.toAnswerStringPart(): String {
  return if (exponent == 1) unit else "^$unit"
}

private fun StringList.toAnswerString(): String {
  return htmlList.joinToString()
}

private fun ListOfSetsOfHtmlStrings.toAnswerString(): String {
  return setOfHtmlStringsList.joinToString { "[${it.toAnswerString()}]" }
}

private fun RatioExpression.toAnswerString(): String {
  return ratioComponentList.joinToString(separator = ":")
}

private fun ImageWithRegions.toAnswerString(): String =
  labelRegionsOrBuilderList.joinToString {
    "[${it.region.regionType} ${it.label} (${it.region.area.upperLeft.x}, " +
      "${it.region.area.upperLeft.y}), (${it.region.area.lowerRight.x}, " +
      "${it.region.area.lowerRight.y})]"
  }

private fun ClickOnImage.toAnswerString(): String =
  "[(${clickedRegionsList.joinToString()}), (${clickPosition.x}, ${clickPosition.y})]"

// https://github.com/oppia/oppia/blob/37285a/core/templates/dev/head/domain/objects/FractionObjectFactory.ts#L47
private fun Fraction.toAnswerString(): String {
  val fractionString = if (numerator != 0) "$numerator/$denominator" else ""
  val mixedString = if (wholeNumber != 0) "$wholeNumber $fractionString" else ""
  val positiveFractionString = if (mixedString.isNotEmpty()) mixedString else fractionString
  val negativeString = if (isNegative) "-" else ""
  return if (positiveFractionString.isNotEmpty()) "$negativeString$positiveFractionString" else "0"
}
