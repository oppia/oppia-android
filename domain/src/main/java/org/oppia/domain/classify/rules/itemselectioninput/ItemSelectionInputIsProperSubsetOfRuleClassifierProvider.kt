package org.oppia.domain.classify.rules.itemselectioninput

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an item selection answer is a proper subset of an input set of
 * values per the item selection input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/ItemSelectionInput/directives/item-selection-input-rules.service.ts#L50
 */
internal class ItemSelectionInputIsProperSubsetOfRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<StringList> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING,
      "x",
      this
    )
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: StringList, input: StringList): Boolean {
    val answerSet = answer.htmlList.toSet()
    val inputSet = input.htmlList.toSet()
    return answerSet.size < inputSet.size && inputSet.containsAll(answerSet)
  }
}
