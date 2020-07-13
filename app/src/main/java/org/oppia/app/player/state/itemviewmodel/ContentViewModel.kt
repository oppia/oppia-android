package org.oppia.app.player.state.itemviewmodel

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  hasBlueBackground: Boolean,
  isCenterAligned: Boolean
) : StateItemViewModel(ViewType.CONTENT) {
  var hasBlueBackground: Boolean = false
  var isCenterAligned: Boolean = false

  init {
    this.hasBlueBackground = hasBlueBackground
    this.isCenterAligned = isCenterAligned
  }
}
