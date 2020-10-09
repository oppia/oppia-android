package org.oppia.android.domain.classify.rules.numericinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an answer is within the input-specified tolerance of another input
 * value per the numeric input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumericInput/directives/numeric-input-rules.service.ts#L41
 */
internal class NumericInputIsWithinToleranceRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.DoubleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      InteractionObject.ObjectTypeCase.REAL,
      "x",
      "tol",
      this
    )
  }

  override fun matches(answer: Double, firstInput: Double, secondInput: Double): Boolean {
    return answer in (firstInput - secondInput)..(firstInput + secondInput)
  }
}
