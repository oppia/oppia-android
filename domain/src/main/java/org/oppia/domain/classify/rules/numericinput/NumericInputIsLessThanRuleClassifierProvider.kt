package org.oppia.domain.classify.rules.numericinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import javax.inject.Inject

/** Provider for a classifier that determines whether an answer is < an input per the numeric input interaction. */
internal class NumericInputIsLessThanRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.REAL, "x", this)
  }

  override fun matches(answer: Double, input: Double): Boolean {
    return answer < input
  }
}
