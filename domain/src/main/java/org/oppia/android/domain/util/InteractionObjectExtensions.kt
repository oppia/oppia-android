package org.oppia.android.domain.util

import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.BOOL_VALUE
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.CLICK_ON_IMAGE
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.FRACTION
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.IMAGE_WITH_REGIONS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NORMALIZED_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.OBJECTTYPE_NOT_SET
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.RATIO_EXPRESSION
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.REAL
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.SIGNED_INT
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.TRANSLATABLE_HTML_CONTENT_ID
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.util.math.toAnswerString

/**
 * Returns a parsable string representation of a user-submitted answer version of this
 * [InteractionObject].
 */
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
    TRANSLATABLE_HTML_CONTENT_ID -> translatableHtmlContentId.toAnswerString()
    SET_OF_TRANSLATABLE_HTML_CONTENT_IDS -> setOfTranslatableHtmlContentIds.toAnswerString()
    LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS ->
      listOfSetsOfTranslatableHtmlContentIds.toAnswerString()
    TRANSLATABLE_SET_OF_NORMALIZED_STRING -> translatableSetOfNormalizedString.toAnswerString()
    OBJECTTYPE_NOT_SET -> "" // The default InteractionObject should be an empty string.
  }
}

/** Returns the set of content IDs corresponding to this container. */
fun SetOfTranslatableHtmlContentIds.getContentIdSet(): Set<String> =
  contentIdsList.map(TranslatableHtmlContentId::getContentId).toSet()

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

private fun ImageWithRegions.toAnswerString(): String =
  labelRegionsOrBuilderList.joinToString {
    "[${it.region.regionType} ${it.label} (${it.region.area.upperLeft.x}, " +
      "${it.region.area.upperLeft.y}), (${it.region.area.lowerRight.x}, " +
      "${it.region.area.lowerRight.y})]"
  }

private fun ClickOnImage.toAnswerString(): String =
  "[(${clickedRegionsList.joinToString()}), (${clickPosition.x}, ${clickPosition.y})]"

private fun TranslatableHtmlContentId.toAnswerString(): String {
  return "content_id=$contentId"
}

private fun SetOfTranslatableHtmlContentIds.toAnswerString(): String {
  return contentIdsList.joinToString()
}

private fun ListOfSetsOfTranslatableHtmlContentIds.toAnswerString(): String {
  return contentIdListsList.joinToString { "[${it.toAnswerString()}]" }
}

private fun TranslatableSetOfNormalizedString.toAnswerString(): String {
  return normalizedStringsList.joinToString()
}
