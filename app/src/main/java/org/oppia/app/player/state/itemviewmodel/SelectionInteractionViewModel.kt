package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/** [ViewModel] for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  val choiceItems: List<String>, val interactionId: String,
  private val interactionAnswerReceiver: InteractionAnswerReceiver, val maxAllowableSelectionCount: Int,
  @Suppress("unused") val minAllowableSelectionCount: Int,
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): ViewModel(), InteractionAnswerHandler {
  val selectedItems = computeSelectedItems(existingAnswer ?: InteractionObject.getDefaultInstance())

  override fun isExplicitAnswerSubmissionRequired(): Boolean {
    // If more than one answer is allowed, then a submission button is needed.
    return maxAllowableSelectionCount > 1
  }

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (interactionId == "ItemSelectionInput") {
      interactionObjectBuilder.setOfHtmlString = StringList.newBuilder()
        .addAllHtml(selectedItems.map(choiceItems::get))
        .build()
    } else if (selectedItems.size == 1) {
      interactionObjectBuilder.nonNegativeInt = selectedItems.first()
    }
    return interactionObjectBuilder.build()
  }

  fun handleItemSelected() {
    // Only push the answer if explicit submission isn't required.
    if (maxAllowableSelectionCount == 1) {
      interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
    }
  }

  private fun computeSelectedItems(answer: InteractionObject): MutableList<Int> {
    return if (interactionId == "ItemSelectionInput") {
      answer.setOfHtmlString.htmlList.map(choiceItems::indexOf).toMutableList()
    } else if (answer.objectTypeCase == InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT) {
      mutableListOf(answer.nonNegativeInt)
    } else {
      mutableListOf()
    }
  }
}
