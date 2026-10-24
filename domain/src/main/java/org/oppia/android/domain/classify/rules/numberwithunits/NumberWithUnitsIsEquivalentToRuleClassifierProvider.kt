package org.oppia.android.domain.classify.rules.numberwithunits

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.normalize
import org.oppia.android.util.math.isApproximatelyEqualTo
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two numbers with units are effectively equal per the number with
 * units interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/NumberWithUnits/directives/number-with-units-rules.service.ts#L48
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class NumberWithUnitsIsEquivalentToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<NumberWithUnits> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS,
      "f",
      this
    )
  }

  override fun matches(
    answer: NumberWithUnits,
    input: NumberWithUnits,
    classificationContext: ClassificationContext
  ): Boolean {
    val normalizedAnswer = answer.normalize() ?: return false
    val normalizedInput = input.normalize() ?: return false
    return normalizedAnswer.unitList.toSet() == normalizedInput.unitList.toSet() &&
      normalizedAnswer.value.isApproximatelyEqualTo(normalizedInput.value)
  }
}
