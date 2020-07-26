package org.oppia.app.player.state.itemviewmodel

/** [StateItemViewModel] for feedback blurbs. */
class FeedbackViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean
) : StateItemViewModel(ViewType.FEEDBACK)
