package org.oppia.domain.classify.rules.fractioninput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has a denominator equal to the specified value per the
 * fraction input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L55
 */
internal class FractionInputHasDenominatorEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.MultiTypeSingleInputMatcher<Fraction, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createMultiTypeSingleInputClassifier(
      InteractionObject.ObjectTypeCase.FRACTION,
      InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      "x",
      this
    )
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Fraction, input: Int): Boolean {
    return answer.denominator == input
  }
}
