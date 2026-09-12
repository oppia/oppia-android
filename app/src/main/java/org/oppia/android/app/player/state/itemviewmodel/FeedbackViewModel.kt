package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for feedback blurbs. */
class FeedbackViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.FEEDBACK) {
  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (other !is FeedbackViewModel) return false
    return htmlContent.toString() == other.htmlContent.toString() &&
      gcsEntityId == other.gcsEntityId &&
      hasConversationView == other.hasConversationView &&
      isSplitView == other.isSplitView &&
      supportsConceptCards == other.supportsConceptCards
  }
}
