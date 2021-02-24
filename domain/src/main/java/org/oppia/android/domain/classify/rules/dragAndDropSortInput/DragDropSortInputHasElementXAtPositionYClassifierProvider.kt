package org.oppia.android.domain.classify.rules.dragAndDropSortInput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.GenericRuleClassifier
import org.oppia.android.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether an element of [ListOfSetsOfHtmlStrings] at a particular position is equal to the specified value per the
 * drag drop sort input interaction.
 *
 * https://github.com/oppia/oppia/blob/03f16147e513ad31cbbf3ce882867a1aac99d649/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L78
 */
// TODO(#1580): Re-restrict access using Bazel visibilities
class DragDropSortInputHasElementXAtPositionYClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<ListOfSetsOfHtmlStrings, String, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      expectedAnswerObjectType = InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
      expectedObjectType1 = InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      firstInputParameterName = "x",
      expectedObjectType2 = InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      secondInputParameterName = "y",
      matcher = this
    )
  }

  /**
   *  We are adding +1 in matching logic to make it even with the web login i.e. input will be of base 1 rather base 0,
   *  which is done to make it human friendly.
   *  */
  override fun matches(
    answer: ListOfSetsOfHtmlStrings,
    firstInput: String,
    secondInput: Int
  ): Boolean {
    return answer.setOfHtmlStringsList.indexOfFirst {
      it.htmlList.contains(firstInput)
    } + 1 == secondInput
  }
}
