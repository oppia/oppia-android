package org.oppia.app.player.exploration

/** [StateItemViewModel] for content-card state. */
class ExplorationContentViewModel(hasBlueBackground: Boolean, isCenterAligned: Boolean) {
  var hasBlueBackground: Boolean = false
  var isCenterAligned: Boolean = false

  init {
    this.hasBlueBackground = hasBlueBackground
    this.isCenterAligned = isCenterAligned
  }
}
