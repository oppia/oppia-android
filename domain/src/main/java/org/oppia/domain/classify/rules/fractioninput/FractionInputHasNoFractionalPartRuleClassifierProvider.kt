package org.oppia.domain.classify.rules.fractioninput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.NoInputClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has no fractional part per the fraction input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L58
 */
internal class FractionInputHasNoFractionalPartRuleClassifierProvider @Inject constructor(
  private val classifierFactory: NoInputClassifier.Factory
): RuleClassifierProvider, NoInputClassifier.NoInputInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.FRACTION, this)
  }

  override fun matches(answer: Fraction): Boolean {
    return answer.numerator == 0
  }
}
