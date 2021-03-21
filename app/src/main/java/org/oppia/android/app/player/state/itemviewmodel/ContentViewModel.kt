package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val contentId: String,
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.CONTENT) {
  val isPlaying = ObservableField<Boolean>(false)

  fun updateIsAudioPlaying(playing: Boolean) = isPlaying.set(playing)
}
