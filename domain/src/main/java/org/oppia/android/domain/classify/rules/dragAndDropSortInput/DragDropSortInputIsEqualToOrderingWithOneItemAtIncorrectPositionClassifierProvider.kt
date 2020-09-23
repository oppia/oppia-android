package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.StringList
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two objects of [ListOfSetsOfHtmlStrings] differ by
 * exactly ordering of one item in the list.
 *
 * https://github.com/oppia/oppia/blob/132b9d8f059253548ea1efadf1ff76416dfa2832/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L72
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider
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
      computeSymmetricDifference(first, second).size
    }.reduce(Int::plus) == 1
  }

  private fun computeSymmetricDifference(
    first: StringList,
    second: StringList
  ): Set<String> {
    val unionOfSetsOfHtmlStrings = unionOfSetsOfHtmlStrings(first, second)
    val intersectOfSetsOfHtmlStrings = intersectOfSetsOfHtmlStrings(first, second)
    return unionOfSetsOfHtmlStrings subtract intersectOfSetsOfHtmlStrings
  }

  private fun unionOfSetsOfHtmlStrings(first: StringList, second: StringList): Set<String> {
    return HashSet(first.htmlList) union HashSet(second.htmlList)
  }

  private fun intersectOfSetsOfHtmlStrings(first: StringList, second: StringList): Set<String> {
    return HashSet(first.htmlList) intersect HashSet(second.htmlList)
  }
}
