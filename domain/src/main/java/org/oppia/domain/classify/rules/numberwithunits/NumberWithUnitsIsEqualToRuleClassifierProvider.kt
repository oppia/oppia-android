package org.oppia.domain.classify.rules.numberwithunits

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberWithUnits
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import org.oppia.domain.util.approximatelyEquals
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are equal per the numbers with units
 * interaction.
 */
internal class NumberWithUnitsIsEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS, "f", this)
  }

  // TODO(#209): Determine whether additional sanitation of the input is necessary here.
  override fun matches(answer: NumberWithUnits, input: NumberWithUnits): Boolean {
    // The number types must match.
    if (answer.numberTypeCase != input.numberTypeCase) {
      return false
    }
    // Units must match, but in different orders is fine.
    if (answer.unitsList.toSet() != input.unitsList.toSet()) {
      return false
    }
    // Otherwise, verify the value itself matches.
    return when (answer.numberTypeCase) {
      NumberWithUnits.NumberTypeCase.REAL -> realMatches(answer.real, input.real)
      NumberWithUnits.NumberTypeCase.FRACTION -> fractionMatches(answer.fraction, input.fraction)
      else -> false // Unknown type never matches.
    }
  }

  private fun realMatches(answer: Float, input: Float): Boolean {
    return input.approximatelyEquals(answer)
  }

  private fun fractionMatches(answer: Fraction, input: Fraction): Boolean {
    return input == answer
  }
}
