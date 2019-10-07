package org.oppia.domain.classify.rules.fractioninput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.MultiTypeSingleInputClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has a numerator equal to the specified value per the
 * fraction input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L52
 */
internal class FractionInputHasNumeratorEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: MultiTypeSingleInputClassifier.Factory
): RuleClassifierProvider, MultiTypeSingleInputClassifier.MultiTypeSingleInputMatcher<Fraction, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(
      InteractionObject.ObjectTypeCase.FRACTION, InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT, "x", this)
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Fraction, input: Int): Boolean {
    return answer.numerator == input
  }
}
