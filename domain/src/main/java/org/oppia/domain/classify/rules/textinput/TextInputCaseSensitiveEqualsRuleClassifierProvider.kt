package org.oppia.domain.classify.rules.textinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import org.oppia.domain.util.normalizeWhitespace
import javax.inject.Inject

/** Determines whether two strings are case sensitively equal per the text input interaction. */
internal class TextInputCaseSensitiveEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider,
  SingleInputClassifier.SingleInputMatcher<String> {
  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.NORMALIZED_STRING, "x", this)
  }

  override fun matches(answer: String, input: String): Boolean {
    return answer.normalizeWhitespace() == input.normalizeWhitespace()
  }
}
