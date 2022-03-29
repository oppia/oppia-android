package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.TRANSLATABLE_HTML_CONTENT_ID
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import org.oppia.android.domain.util.getContentIdSet
import javax.inject.Inject

// Note: the number is needed due to https://youtrack.jetbrains.com/issue/KT-24700 to avoid a
// redeclaration error with other files.
private typealias ContentId2 = TranslatableHtmlContentId
private typealias ListOfContentIdSets2 = ListOfSetsOfTranslatableHtmlContentIds

/**
 * Provider for a classifier that determines whether position of an element of
 * [ListOfSetsOfTranslatableHtmlContentIds] at a particular position is less than position of
 * specified element as per the drag drop sort input interaction.
 *
 * https://github.com/oppia/oppia/blob/132b9d8f059253548ea1efadf1ff76416dfa2832/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L88
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputHasElementXBeforeElementYClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<ListOfContentIdSets2, ContentId2, ContentId2> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      expectedAnswerObjectType = LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS,
      expectedObjectType1 = TRANSLATABLE_HTML_CONTENT_ID,
      firstInputParameterName = "x",
      expectedObjectType2 = TRANSLATABLE_HTML_CONTENT_ID,
      secondInputParameterName = "y",
      matcher = this
    )
  }

  override fun matches(
    answer: ListOfContentIdSets2,
    firstInput: ContentId2,
    secondInput: ContentId2,
    classificationContext: ClassificationContext
  ): Boolean {
    val answerSets = answer.contentIdListsList.map { it.getContentIdSet() }
    return answerSets.indexOfFirst {
      firstInput.contentId in it
    } < answerSets.indexOfFirst {
      secondInput.contentId in it
    }
  }
}
