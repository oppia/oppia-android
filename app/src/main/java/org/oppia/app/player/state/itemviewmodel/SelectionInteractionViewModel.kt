package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.player.state.listener.InteractionAnswerRetriever
import javax.inject.Inject

/** [ViewModel] for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  val choiceItems: List<String>, val interactionId: String, val maxAllowableSelectionCount: Int,
  @Suppress("unused") val minAllowableSelectionCount: Int,
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): ViewModel(), InteractionAnswerRetriever {
  val selectedItems = computeSelectedItems(existingAnswer ?: InteractionObject.getDefaultInstance())

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
