package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val contentId: String,
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.CONTENT) {
  val isAudioPlaying = ObservableBoolean(false)

  fun updateIsAudioPlaying(isPlaying: Boolean){
    isAudioPlaying.set(isPlaying)
  }
}
