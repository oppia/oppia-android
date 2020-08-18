package org.oppia.domain.classify.rules.multiplechoiceinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a multiple choice answer matches a specific option per the multiple
 * choice input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/MultipleChoiceInput/directives/multiple-choice-input-rules.service.ts#L21
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class MultipleChoiceInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      "x",
      this
    )
  }

  override fun matches(answer: Int, input: Int): Boolean {
    return answer == input
  }
}
