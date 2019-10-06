package org.oppia.domain.classify.rules.numericinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.DoubleInputClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an answer is >= one input and <= another input value per the
 * numeric input interaction.
 */
internal class NumericInputIsInclusivelyBetweenRuleClassifierProvider @Inject constructor(
  private val classifierFactory: DoubleInputClassifier.Factory
): RuleClassifierProvider, DoubleInputClassifier.DoubleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.REAL, "a", "b", this)
  }

  override fun matches(answer: Double, firstInput: Double, secondInput: Double): Boolean {
    return answer in firstInput..secondInput
  }
}
