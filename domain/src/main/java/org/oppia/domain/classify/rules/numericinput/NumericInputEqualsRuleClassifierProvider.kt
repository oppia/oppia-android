package org.oppia.domain.classify.rules.numericinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import org.oppia.domain.util.approximatelyEquals
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two integers are equal per the numeric input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumericInput/directives/numeric-input-rules.service.ts#L21
 */
internal class NumericInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.REAL, "x", this)
  }

  override fun matches(answer: Double, input: Double): Boolean {
    return input.approximatelyEquals(answer)
  }
}
