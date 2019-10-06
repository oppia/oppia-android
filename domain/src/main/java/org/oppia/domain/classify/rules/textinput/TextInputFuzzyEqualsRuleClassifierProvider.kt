package org.oppia.domain.classify.rules.textinput

import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.classify.rules.SingleInputClassifier
import javax.inject.Inject

/**
 * Determines whether two strings are fuzzily equal per the text input interaction. For context, see the Oppia web
 * version:
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/TextInput/directives/text-input-rules.service.ts#L29.
 */
internal class TextInputFuzzyEqualsRuleClassifierProvider @Inject constructor(
  private val classifierFactory: SingleInputClassifier.Factory
): RuleClassifierProvider,
  SingleInputClassifier.SingleInputMatcher<String> {
  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.create(InteractionObject.ObjectTypeCase.NORMALIZED_STRING, "x", this)
  }

  override fun matches(answer: String, input: String): Boolean {
    val lowerInput = input.toLowerCase()
    val lowerAnswer = answer.toLowerCase()
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
        if (lowerInput[i - 1] == lowerAnswer[j - 1]) {
          editDistance[i][j] = editDistance[i - 1][j - 1];
        } else {
          editDistance[i][j] = minOf(
            editDistance[i - 1][j - 1],
            editDistance[i][j - 1],
            editDistance[i - 1][j]
          ) + 1;
        }
      }
    }
    return editDistance[lowerInput.length][lowerAnswer.length] == 1;
  }
}

