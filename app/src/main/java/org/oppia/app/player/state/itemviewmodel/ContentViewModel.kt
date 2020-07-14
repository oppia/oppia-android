package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val contentId: String,
  val htmlContent: CharSequence,
  val gcsEntityId: String
) : StateItemViewModel(ViewType.CONTENT) {
  val isAudioPlaying = ObservableField<Boolean>(false)

  fun updateIsAudioPlaying(isPlaying: Boolean) {
    isAudioPlaying.set(isPlaying)
  }
}
