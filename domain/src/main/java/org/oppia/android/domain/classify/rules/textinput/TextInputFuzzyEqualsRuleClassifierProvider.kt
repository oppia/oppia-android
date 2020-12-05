package org.oppia.android.domain.classify.rules.textinput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.normalizeWhitespace
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two strings are fuzzily equal per the text input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L29.
 */
internal class TextInputFuzzyEqualsRuleClassifierProvider @Inject constructor(
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
    val lowerInput = input.normalizeWhitespace().toLowerCase()
    val lowerAnswer = answer.normalizeWhitespace().toLowerCase()
    if (lowerInput == lowerAnswer) {
      return true
    }

    val editDistance = mutableListOf<MutableList<Int>>()
    for (i in 0..lowerInput.length) {
      editDistance.add(mutableListOf(i))
    }
    for (j in 1..lowerAnswer.length) {
      editDistance[0].add(j)
    }

    for (i in 1..lowerInput.length) {
      for (j in 1..lowerAnswer.length) {
        check(j == editDistance[i].size) {
          "Something went wrong. Expected index $j to not yet be in array: ${editDistance[i]}"
        }
        if (lowerInput[i - 1] == lowerAnswer[j - 1]) {
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
    return editDistance[lowerInput.length][lowerAnswer.length] == 1
  }
}
