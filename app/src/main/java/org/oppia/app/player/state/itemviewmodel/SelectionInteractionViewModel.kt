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
  @Suppress("unused") val minAllowableSelectionCount: Int
): ViewModel(), InteractionAnswerRetriever {
  val selectedItems = mutableListOf<Int>()

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
}
