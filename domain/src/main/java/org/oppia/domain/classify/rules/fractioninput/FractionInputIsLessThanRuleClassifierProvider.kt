package org.oppia.domain.classify.rules.fractioninput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import org.oppia.domain.util.toFloat
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction is less than another fraction per the fraction input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L42
 */
internal class FractionInputIsLessThanRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.FRACTION, "f", this)
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Fraction, input: Fraction): Boolean {
    return answer.toFloat() < input.toFloat()
  }
}
