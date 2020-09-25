package org.oppia.android.domain.classify.rules.numberwithunits

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.approximatelyEquals
import org.oppia.android.domain.util.toFloat
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are effectively equal per the number with
 * units interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumberWithUnits/directives/number-with-units-rules.service.ts#L48
 */
internal class NumberWithUnitsIsEquivalentToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS,
      "f",
      this
    )
  }

  // TODO(#209): Determine whether additional normalization of the input is necessary here.
  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: NumberWithUnits, input: NumberWithUnits): Boolean {
    // Units must match, but in different orders is fine.
    if (answer.unitList.toSet() != input.unitList.toSet()) {
      return false
    }

    // Verify the float version of the value for approximate comparison.
    return extractRealValue(input).approximatelyEquals(extractRealValue(answer))
  }

  private fun extractRealValue(number: NumberWithUnits): Double {
    return when (number.numberTypeCase) {
      NumberWithUnits.NumberTypeCase.REAL -> number.real
      NumberWithUnits.NumberTypeCase.FRACTION -> number.fraction.toFloat().toDouble()
      else -> throw IllegalArgumentException("Invalid number type: ${number.numberTypeCase.name}")
    }
  }
}
