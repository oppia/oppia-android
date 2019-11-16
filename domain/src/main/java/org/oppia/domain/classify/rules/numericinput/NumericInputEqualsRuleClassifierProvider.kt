package org.oppia.domain.classify.rules.numericinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.util.approximatelyEquals
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two integers are equal per the numeric input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumericInput/directives/numeric-input-rules.service.ts#L21
 */
internal class NumericInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
): RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(InteractionObject.ObjectTypeCase.REAL, "x", this)
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Double, input: Double): Boolean {
    return input.approximatelyEquals(answer)
  }
}
