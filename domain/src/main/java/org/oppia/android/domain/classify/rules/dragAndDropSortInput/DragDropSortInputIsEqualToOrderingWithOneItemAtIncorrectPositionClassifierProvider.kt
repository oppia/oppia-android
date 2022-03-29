package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.getContentIdSet
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two objects of
 * [ListOfSetsOfTranslatableHtmlContentIds] differ by exactly ordering of one item in the list.
 *
 * https://github.com/oppia/oppia/blob/132b9d8f059253548ea1efadf1ff76416dfa2832/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L72
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider
@Inject constructor(
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
  ): Boolean {
    val answerStringSets = answer.contentIdListsList
    val inputStringSets = input.contentIdListsList
    return (answerStringSets zip inputStringSets).map { (first, second) ->
      computeSymmetricDifference(first.getContentIdSet(), second.getContentIdSet()).size
    }.reduce(Int::plus) == 1
  }

  /**
   * Returns the symmetric difference of the two sets. That is, the set of elements that are
   * individually one of either sets, but not both.
   */
  private fun computeSymmetricDifference(first: Set<String>, second: Set<String>): Set<String> {
    return (first union second) subtract (first intersect second)
  }
}
