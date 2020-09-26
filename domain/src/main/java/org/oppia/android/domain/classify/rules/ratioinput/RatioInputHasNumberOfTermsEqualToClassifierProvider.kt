package org.oppia.android.domain.classify.rules.ratioinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two object have an equal number of terms.
 */
internal class RatioInputHasNumberOfTermsEqualToClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeSingleInputMatcher<RatioExpression, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createMultiTypeSingleInputClassifier(
      InteractionObject.ObjectTypeCase.RATIO_EXPRESSION,
      InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      "y",
      this
    )
  }

  override fun matches(answer: RatioExpression, input: Int): Boolean {
    return answer.ratioComponentCount == input
  }
}
