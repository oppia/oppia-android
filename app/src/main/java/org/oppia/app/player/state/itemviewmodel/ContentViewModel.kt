package org.oppia.app.player.state.itemviewmodel

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  hasConversationalContentView: Boolean
) : StateItemViewModel(ViewType.CONTENT) {
  var hasConversationalContentView: Boolean = false

  init {
    this.hasConversationalContentView = hasConversationalContentView
  }
}
