package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.extensions.normalizeWhitespace
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two strings are fuzzily equal per the text
 * input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L29.
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class TextInputFuzzyEqualsRuleClassifierProvider @Inject constructor(
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
    classificationContext: ClassificationContext
  ): Boolean {
    val inputStringList =
      translationController.extractStringList(
        input, classificationContext.writtenTranslationContext
      )
    return inputStringList.any { hasEditDistanceEqualToOne(it, answer) }
  }

  private fun hasEditDistanceEqualToOne(inputString: String, matchString: String): Boolean {
    val lowerInput = machineLocale.run { inputString.normalizeWhitespace().toMachineLowerCase() }
    val lowerMatch = machineLocale.run { matchString.normalizeWhitespace().toMachineLowerCase() }
    if (lowerInput == lowerMatch) {
      return true
    }

    val editDistance = mutableListOf<MutableList<Int>>()
    for (i in 0..lowerInput.length) {
      editDistance.add(mutableListOf(i))
    }
    for (j in 1..lowerMatch.length) {
      editDistance[0].add(j)
    }

    for (i in 1..lowerInput.length) {
      for (j in 1..lowerMatch.length) {
        check(j == editDistance[i].size) {
          "Something went wrong. Expected index $j to not yet be in array: ${editDistance[i]}"
        }
        if (lowerInput[i - 1] == lowerMatch[j - 1]) {
          editDistance[i].add(editDistance[i - 1][j - 1])
        } else {
          editDistance[i].add(
            minOf(
              editDistance[i - 1][j - 1],
              editDistance[i][j - 1],
              editDistance[i - 1][j]
            ) + 1
          )
        }
      }
    }
    return editDistance[lowerInput.length][lowerMatch.length] == 1
  }
}
