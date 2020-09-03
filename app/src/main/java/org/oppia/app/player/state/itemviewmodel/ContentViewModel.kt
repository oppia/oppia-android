package org.oppia.app.player.state.itemviewmodel

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.CONTENT)
