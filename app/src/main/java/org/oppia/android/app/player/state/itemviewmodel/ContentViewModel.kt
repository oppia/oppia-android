package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.CONTENT) {
  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (this === other) return true
    if (other !is ContentViewModel) return false

    return htmlContent == other.htmlContent &&
      gcsEntityId == other.gcsEntityId &&
      hasConversationView == other.hasConversationView &&
      isSplitView == other.isSplitView &&
      supportsConceptCards == other.supportsConceptCards
  }
}
