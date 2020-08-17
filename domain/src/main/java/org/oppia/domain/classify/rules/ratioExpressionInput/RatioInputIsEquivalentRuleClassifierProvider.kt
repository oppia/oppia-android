package org.oppia.domain.classify.rules.ratioExpressionInput

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.RatioExpression
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.util.gcd
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two object are equal by converting them into
 * their lowest as per the ratio input interaction.
 *
 */
internal class RatioInputIsEquivalentRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<RatioExpression> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.RATIO_EXPRESSION,
      "x",
      this
    )
  }

  override fun matches(answer: RatioExpression, input: RatioExpression): Boolean {
    return answer.html == getLowestForm(input)
  }

  private fun getLowestForm(input: RatioExpression): String {
    val ratios = input.html.split(':').map { it.toInt() }
    val value = ratios.reduce { x, y -> gcd(x, y) }
    return if (value == 1) {
      input.html
    } else {
      ratios.map { x -> x / value }.joinToString(separator = ":") { it.toString() }
    }
  }
}
