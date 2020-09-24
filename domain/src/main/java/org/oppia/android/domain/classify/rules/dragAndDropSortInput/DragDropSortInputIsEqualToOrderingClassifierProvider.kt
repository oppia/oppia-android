package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.StringList
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two objects of [ListOfSetsOfHtmlStrings] are equal by checking their every list of both objects at every index
 *
 * https://github.com/oppia/oppia/blob/7d15d813ae6577367a5884af4beb0d6995f19251/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L65
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputIsEqualToOrderingClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<ListOfSetsOfHtmlStrings> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
      "x",
      this
    )
  }

  override fun matches(answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings): Boolean {
    return answer.setOfHtmlStringsCount == input.setOfHtmlStringsCount && (
      areListOfSetsOfHtmlStringsEqual(
        answer, input
      )
      )
  }

  /**
   * This functions checks the equality of two nested lists irrespective of positions of nested list only ordering of first list matters
   * It returns true if all the list items are equal and are at correct position otherwise false
   */
  private fun areListOfSetsOfHtmlStringsEqual(
    answer: ListOfSetsOfHtmlStrings,
    input: ListOfSetsOfHtmlStrings
  ): Boolean {
    /*
     * For Ex - list1 = [a, b, c] & list2 = [1, 2, 3]
     *          list1 zip list2 => [{a, 1}, {b, 2}, {b, 3}]
     *          then two items are checked using areSetsOfHtmlStringsEqual which gives a boolean value
     *          then reduce operator on a list of boolean for ex [false, true, false, false, false]
     *          will result in  val a = false && true && false && false && false where && signifies the operator
     *          which is meant to be applied.
     */
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
