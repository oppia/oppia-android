package org.oppia.app.player.exploration

/** [ViewModel] for [StatePlayerRecyclerViewAssembler] items. */
class ExplorationContentViewModel(hasBlueBackground: Boolean, isCenterAligned: Boolean) {
  var hasBlueBackground: Boolean = false
  var isCenterAligned: Boolean = false

  init {
    this.hasBlueBackground = hasBlueBackground
    this.isCenterAligned = isCenterAligned
  }
}
