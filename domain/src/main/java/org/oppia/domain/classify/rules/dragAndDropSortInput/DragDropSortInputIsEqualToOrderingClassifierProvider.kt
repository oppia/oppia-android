package org.oppia.domain.classify.rules.dragAndDropSortInput

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two objects of [ListOfSetsOfHtmlStrings] are equal by checking their every list of both objects at every index
 *
 * https://github.com/oppia/oppia/blob/7d15d813ae6577367a5884af4beb0d6995f19251/extensions/interactions/DragAndDropSortInput/directives/drag-and-drop-sort-input-rules.service.ts#L65
 */
internal class DragDropSortInputIsEqualToOrderingClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<ListOfSetsOfHtmlStrings> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING, "x", this
    )
  }

  override fun matches(answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings): Boolean {
    return answer.setOfHtmlStringsCount == input.setOfHtmlStringsCount && (checkEquality(
      answer, input
    ))
  }

  private fun checkEquality(
    answer: ListOfSetsOfHtmlStrings, input: ListOfSetsOfHtmlStrings
  ): Boolean {
    for (i in 0  until answer.setOfHtmlStringsCount) {
      if (answer.getSetOfHtmlStrings(i).htmlCount == input.getSetOfHtmlStrings(i).htmlCount) {
        for ( j in 0 until answer.getSetOfHtmlStrings(i).htmlCount) {
          if (input.getSetOfHtmlStrings(i).htmlList.indexOf(answer.getSetOfHtmlStrings(i).getHtml(j)) == -1) {
            return false
          }
        }
      } else {
        return false
      }
    }
    return true
  }
}