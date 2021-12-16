package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.getContentIdSet
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two objects of
 * [ListOfSetsOfTranslatableHtmlContentIds] are equal by checking their every list of both objects
 * at every index.
 *
 * https://github.com/oppia/oppia/blob/7d15d813ae6577367a5884af4beb0d6995f19251/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L65
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputIsEqualToOrderingClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.SingleInputMatcher<ListOfSetsOfTranslatableHtmlContentIds> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      expectedObjectType = LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS,
      inputParameterName = "x",
      matcher = this
    )
  }

  override fun matches(
    answer: ListOfSetsOfTranslatableHtmlContentIds,
    input: ListOfSetsOfTranslatableHtmlContentIds,
    classificationContext: ClassificationContext
  ): Boolean = areListOfSetsOfHtmlStringsEqual(answer, input)

  /**
   * Returns whether the two specified lists are equal irrespective of the item positions in each
   * list.
   */
  private fun areListOfSetsOfHtmlStringsEqual(
    answer: ListOfSetsOfTranslatableHtmlContentIds,
    input: ListOfSetsOfTranslatableHtmlContentIds
  ): Boolean {
    /*
     * For Ex - list1 = [a, b, c] & list2 = [1, 2, 3]
     *          list1 zip list2 => [{a, 1}, {b, 2}, {b, 3}]
     *          then two items are checked using areSetsOfHtmlStringsEqual which gives a boolean value
     *          then reduce operator on a list of boolean for ex [false, true, false, false, false]
     *          will result in  val a = false && true && false && false && false where && signifies the operator
     *          which is meant to be applied.
     */
    if (answer.contentIdListsCount != input.contentIdListsCount) {
      return false
    }
    val answerStringSets = answer.contentIdListsList
    val inputStringSets = input.contentIdListsList
    return (answerStringSets zip inputStringSets).map { (first, second) ->
      areSetsOfHtmlStringsEqual(first, second)
    }.reduce(Boolean::and)
  }

  private fun areSetsOfHtmlStringsEqual(
    first: SetOfTranslatableHtmlContentIds,
    second: SetOfTranslatableHtmlContentIds
  ): Boolean = first.getContentIdSet() == second.getContentIdSet()
}
