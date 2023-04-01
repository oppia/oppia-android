package org.oppia.android.domain.classify.rules.ratioinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a particular component within an answer ratio
 * has a specific value.
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class RatioInputHasSpecificTermEqualToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<RatioExpression, Int, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      expectedAnswerObjectType = InteractionObject.ObjectTypeCase.RATIO_EXPRESSION,
      expectedObjectType1 = InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      firstInputParameterName = "x",
      expectedObjectType2 = InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      secondInputParameterName = "y",
      matcher = this
    )
  }

  override fun matches(
    answer: RatioExpression,
    firstInput: Int,
    secondInput: Int,
    classificationContext: ClassificationContext
  ): Boolean = answer.ratioComponentList.getOrNull(firstInput - 1) == secondInput
}
