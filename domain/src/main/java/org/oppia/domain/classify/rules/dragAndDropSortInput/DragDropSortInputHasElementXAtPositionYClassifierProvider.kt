package org.oppia.domain.classify.rules.dragAndDropSortInput

import javax.inject.Inject
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider

/**
 * Provider for a classifier that determines whether an element of [ListOfSetsOfHtmlStrings] at a particular position is equal to the specified value per the
 * drag drop sort input interaction.
 *
 * https://github.com/oppia/oppia/blob/03f16147e513ad31cbbf3ce882867a1aac99d649/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L78
 */
internal class DragDropSortInputHasElementXAtPositionYClassifierProvider @Inject constructor(
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

  override fun matches(
    answer: ListOfSetsOfHtmlStrings,
    firstInput: String,
    secondInput: Int
  ): Boolean {
    return answer.setOfHtmlStringsList.indexOfFirst {
      it.htmlList.contains(firstInput)
    } == secondInput
  }
}
