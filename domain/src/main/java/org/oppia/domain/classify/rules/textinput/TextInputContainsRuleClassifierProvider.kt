package org.oppia.domain.classify.rules.textinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.util.normalizeWhitespace
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an answer contains the rule's input per the text input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L70
 */
internal class TextInputContainsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<String> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      "x",
      this
    )
  }

  override fun matches(answer: String, input: String): Boolean {
    return answer.normalizeWhitespace().contains(input.normalizeWhitespace())
  }
}
