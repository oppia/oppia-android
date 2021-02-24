package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for feedback blurbs. */
class FeedbackViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.FEEDBACK)
