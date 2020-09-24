package org.oppia.domain.classify.rules.fractioninput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has a fractional part exactly equal to the fractional
 * part of an input fraction per the fraction input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L61
 */
internal class FractionInputHasFractionalPartExactlyEqualToRuleClassifierProvider
@Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.FRACTION,
      "f",
      this
    )
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Fraction, input: Fraction): Boolean {
    return answer.numerator == input.numerator && answer.denominator == input.denominator
  }
}
