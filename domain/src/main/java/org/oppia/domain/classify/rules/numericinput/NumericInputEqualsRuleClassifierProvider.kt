package org.oppia.domain.classify.rules.numericinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import javax.inject.Inject
import kotlin.math.abs

private const val EPSILON = 1e-5

/** Provider for a classifier that determines whether two integers are equal per the numeric input interaction. */
internal class NumericInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider, SingleInputClassifier.SingleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.REAL, "x", this)
  }

  override fun matches(answer: Double, input: Double): Boolean {
    return abs(answer - input) < EPSILON
  }
}
