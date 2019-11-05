package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel

/** [ViewModel] for multiple or item-selection input choice list. */
class SelectionInteractionCustomizationArgsViewModel : ViewModel() {
  var choiceItems: MutableList<String>? = null
  var interactionId: String = ""
  var maxAllowableSelectionCount: Int = 0
  var minAllowableSelectionCount: Int = 0
}
