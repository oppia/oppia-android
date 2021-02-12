package org.oppia.android.domain.classify.rules.fractioninput

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has no fractional part per the fraction input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L58
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class FractionInputHasNoFractionalPartRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.NoInputInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createNoInputClassifier(
      InteractionObject.ObjectTypeCase.FRACTION,
      this
    )
  }

  override fun matches(answer: Fraction): Boolean {
    return answer.numerator == 0
  }
}
