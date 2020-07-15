package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel

/** [ViewModel] for [StatePlayerRecyclerViewAssembler] items. */
class PlayerRecyclerViewAssemblerViewModel(hasBlueBackground: Boolean, isCenterAligned: Boolean)  : ViewModel() {
  var hasBlueBackground: Boolean = false
  var isCenterAligned: Boolean = false

  init {
    this.hasBlueBackground = hasBlueBackground
    this.isCenterAligned = isCenterAligned
  }
}
