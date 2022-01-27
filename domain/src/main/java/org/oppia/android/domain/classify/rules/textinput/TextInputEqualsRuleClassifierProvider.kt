package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.extensions.normalizeWhitespace
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two strings are equal per the text input
 * interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L24
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class TextInputEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val translationController: TranslationController
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

  override fun matches(
    answer: String,
    input: TranslatableSetOfNormalizedString,
    writtenTranslationContext: WrittenTranslationContext
  ): Boolean {
    val normalizedAnswer = answer.normalizeWhitespace()
    val inputStringList = translationController.extractStringList(input, writtenTranslationContext)
    return inputStringList.any {
      machineLocale.run { it.normalizeWhitespace().equalsIgnoreCase(normalizedAnswer) }
    }
  }
}
