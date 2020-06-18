package org.oppia.domain.classify.rules.dragAndDropSortInput

import javax.inject.Inject
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider

/**
 * Provider for a classifier that determines whether two objects of [ListOfSetsOfHtmlStrings] differ by
 * exactly ordering of one item in the list.
 *
 * https://github.com/oppia/oppia/blob/132b9d8f059253548ea1efadf1ff76416dfa2832/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L72
 */
internal class DragDropSortInputIsEqualToOrderingWithOneItemIncorrectClassifierProvider
@Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.SingleInputMatcher<ListOfSetsOfHtmlStrings> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      expectedObjectType = InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
      inputParameterName = "x",
      matcher = this
    )
  }

  override fun matches(answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings): Boolean {
    val answerStringSets = answer.setOfHtmlStringsList
    val inputStringSets = input.setOfHtmlStringsList
    return (answerStringSets zip inputStringSets).map { (first, second) ->
      checkEqualityWithIncorrectPositions(first, second).size
    }.reduce(Int::plus) == 1
  }

  private fun checkEqualityWithIncorrectPositions(
    first: StringList,
    second: StringList
  ): Set<String> {
    return (unionOfSetsOfHtmlStrings(first, second) subtract intersectOfSetsOfHtmlStrings(
      first,
      second
    ))
  }

  private fun unionOfSetsOfHtmlStrings(first: StringList, second: StringList): Set<String> {
    return HashSet(first.htmlList) union HashSet(second.htmlList)
  }

  private fun intersectOfSetsOfHtmlStrings(first: StringList, second: StringList): Set<String> {
    return HashSet(first.htmlList) intersect HashSet(second.htmlList)
  }
}
