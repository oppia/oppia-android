package org.oppia.android.domain.classify.rules.numberwithunits

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.approximatelyEquals
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are equal per the numbers with units
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumberWithUnits/directives/number-with-units-rules.service.ts#L34
 */
internal class NumberWithUnitsIsEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS,
      "f",
      this
    )
  }

  // TODO(#209): Determine whether additional sanitation of the input is necessary here.
  override fun matches(answer: NumberWithUnits, input: NumberWithUnits): Boolean {
    // The number types must match.
    if (answer.numberTypeCase != input.numberTypeCase) {
      return false
    }
    // Units must match, but in different orders is fine.
    if (answer.unitList.toSet() != input.unitList.toSet()) {
      return false
    }
    // Otherwise, verify the value itself matches.
    return when (answer.numberTypeCase) {
      NumberWithUnits.NumberTypeCase.REAL -> realMatches(answer.real, input.real)
      NumberWithUnits.NumberTypeCase.FRACTION -> fractionMatches(answer.fraction, input.fraction)
      else -> false // Unknown type never matches.
    }
  }

  private fun realMatches(answer: Double, input: Double): Boolean {
    return input.approximatelyEquals(answer)
  }

  private fun fractionMatches(answer: Fraction, input: Fraction): Boolean {
    return input == answer
  }
}
