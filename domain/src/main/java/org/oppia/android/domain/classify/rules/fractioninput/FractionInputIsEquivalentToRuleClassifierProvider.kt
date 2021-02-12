package org.oppia.android.domain.classify.rules.fractioninput

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.approximatelyEquals
import org.oppia.android.domain.util.toFloat
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction effectively equal to another fraction per the fraction
 * input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L29
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class FractionInputIsEquivalentToRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.FRACTION,
      "f",
      this
    )
  }

  override fun matches(answer: Fraction, input: Fraction): Boolean {
    return answer.toFloat().approximatelyEquals(input.toFloat())
  }
}
