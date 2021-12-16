package org.oppia.android.domain.classify.rules.numericinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an answer is >= one input and <= another input value per the
 * numeric input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumericInput/directives/numeric-input-rules.service.ts#L36
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class NumericInputIsInclusivelyBetweenRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.DoubleInputMatcher<Double> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      InteractionObject.ObjectTypeCase.REAL,
      "a",
      "b",
      this
    )
  }

  override fun matches(
    answer: Double,
    firstInput: Double,
    secondInput: Double,
    classificationContext: ClassificationContext
  ): Boolean = answer in firstInput..secondInput
}
