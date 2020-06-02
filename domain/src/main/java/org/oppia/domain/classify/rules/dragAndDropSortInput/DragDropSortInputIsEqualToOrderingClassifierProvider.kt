package org.oppia.domain.classify.rules.dragAndDropSortInput

import javax.inject.Inject
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider

/**
 * Provider for a classifier that determines whether two objects of [ListOfSetsOfHtmlStrings] are equal by checking their every list of both objects at every index
 *
 * https://github.com/oppia/oppia/blob/7d15d813ae6577367a5884af4beb0d6995f19251/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L65
 */
internal class DragDropSortInputIsEqualToOrderingClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<ListOfSetsOfHtmlStrings> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING, "x", this
    )
  }

  override fun matches(answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings): Boolean {
    return answer.setOfHtmlStringsCount == input.setOfHtmlStringsCount && (areListOfSetsOfHtmlStringsEqual(
      answer, input
    ))
  }

  private fun areListOfSetsOfHtmlStringsEqual(answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings): Boolean {
    if (answer.setOfHtmlStringsCount != input.setOfHtmlStringsCount) {
      return false
    }
    val answerStringSets = answer.setOfHtmlStringsList
    val inputStringSets = input.setOfHtmlStringsList
    return (answerStringSets zip inputStringSets).map { (first, second) ->
      areSetsOfHtmlStringsEqual(first, second)
    }.reduce(Boolean::and)
  }

  private fun areSetsOfHtmlStringsEqual(first: StringList, second: StringList): Boolean {
    return HashSet(first.htmlList) == HashSet(second.htmlList)
  }
}
