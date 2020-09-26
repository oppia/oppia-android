package org.oppia.android.domain.classify.rules.fractioninput

import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.approximatelyEquals
import org.oppia.android.domain.util.toFloat
import org.oppia.android.domain.util.toSimplestForm
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction is both effectively equal to another fraction and equal
 * in its simplest form per the fraction input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L32
 */
internal class FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider
@Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<Fraction> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.FRACTION,
      "f",
      this
    )
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: Fraction, input: Fraction): Boolean {
    return answer.toFloat().approximatelyEquals(input.toFloat()) && answer == input.toSimplestForm()
  }
}
