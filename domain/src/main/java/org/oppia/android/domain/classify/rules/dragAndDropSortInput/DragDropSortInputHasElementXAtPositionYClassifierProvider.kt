package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS
import org.oppia.android.app.model.InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT
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
private typealias ContentId1 = TranslatableHtmlContentId
private typealias ListOfContentIdSets1 = ListOfSetsOfTranslatableHtmlContentIds

/**
 * Provider for a classifier that determines whether an element of
 * [ListOfSetsOfTranslatableHtmlContentIds] at a particular position is equal to the specified value
 * per the drag drop sort input interaction.
 *
 * https://github.com/oppia/oppia/blob/03f16147e513ad31cbbf3ce882867a1aac99d649/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L78
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputHasElementXAtPositionYClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<ListOfContentIdSets1, ContentId1, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      expectedAnswerObjectType = LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS,
      expectedObjectType1 = TRANSLATABLE_HTML_CONTENT_ID,
      firstInputParameterName = "x",
      expectedObjectType2 = NON_NEGATIVE_INT,
      secondInputParameterName = "y",
      matcher = this
    )
  }

  override fun matches(
    answer: ListOfContentIdSets1,
    firstInput: ContentId1,
    secondInput: Int,
    classificationContext: ClassificationContext
  ): Boolean {
    // Note that the '1' returned here is to have consistency with the web platform: matched indexes
    // start at 1 rather than 0 to make the indexes more human friendly.
    return answer.contentIdListsList.indexOfFirst {
      firstInput.contentId in it.getContentIdSet()
    } + 1 == secondInput
  }
}
