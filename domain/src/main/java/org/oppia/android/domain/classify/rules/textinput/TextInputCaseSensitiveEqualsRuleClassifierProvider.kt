package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.normalizeWhitespace
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two strings are case sensitively equal per the text input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L59
 */
internal class TextInputCaseSensitiveEqualsRuleClassifierProvider @Inject constructor(
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
    return answer.normalizeWhitespace() == input.normalizeWhitespace()
  }
}
