package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.normalizeWhitespace
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two strings are equal per the text input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L24
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class TextInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeSingleInputMatcher<String, TranslatableSetOfNormalizedString> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createMultiTypeSingleInputClassifier(
      InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      InteractionObject.ObjectTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING,
      "x",
      this
    )
  }

  override fun matches(answer: String, input: TranslatableSetOfNormalizedString): Boolean {
    val normalizedAnswer = answer.normalizeWhitespace()
    return input.normalizedStringsList.any {
      it.normalizeWhitespace().equals(normalizedAnswer, ignoreCase = true)
    }
  }
}
