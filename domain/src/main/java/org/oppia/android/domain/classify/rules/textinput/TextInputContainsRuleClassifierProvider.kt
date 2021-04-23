package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.normalizeWhitespace
import java.util.Locale
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an answer contains the rule's input per the
 * text input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L70
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class TextInputContainsRuleClassifierProvider @Inject constructor(
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
    val normalizedAnswer = answer.normalizeWhitespace().toLowerCase(Locale.getDefault())
    return input.normalizedStringsList.any {
      normalizedAnswer.contains(it.normalizeWhitespace().toLowerCase(Locale.getDefault()))
    }
  }
}
