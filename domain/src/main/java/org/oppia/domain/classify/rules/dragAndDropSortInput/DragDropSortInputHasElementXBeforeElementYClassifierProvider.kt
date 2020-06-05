package org.oppia.domain.classify.rules.dragAndDropSortInput

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether position of an element of [ListOfSetsOfHtmlStrings] at a particular position is less than position of specified element as per the
 * drag drop sort input interaction.
 *
 * https://github.com/oppia/oppia/blob/132b9d8f059253548ea1efadf1ff76416dfa2832/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L88
 */
internal class DragDropSortInputHasElementXBeforeElementYClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<ListOfSetsOfHtmlStrings, String, String> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      expectedAnswerObjectType = InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
      expectedObjectType1 = InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      firstInputParameterName = "x",
      expectedObjectType2 = InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      secondInputParameterName = "y",
      matcher = this
    )
  }

  override fun matches(
    answer: ListOfSetsOfHtmlStrings,
    firstInput: String,
    secondInput: String
  ): Boolean {
    return answer.setOfHtmlStringsList.indexOfFirst {
      it.htmlList.contains(firstInput)
    } < answer.setOfHtmlStringsList.indexOfFirst {
      it.htmlList.contains(secondInput)
    }
  }
}
