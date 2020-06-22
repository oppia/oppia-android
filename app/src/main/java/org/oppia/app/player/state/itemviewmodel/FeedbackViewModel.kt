package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField

/** [StateItemViewModel] for feedback blurbs. */
class FeedbackViewModel(val contentId: String, val htmlContent: CharSequence, val gcsEntityId: String) : StateItemViewModel(ViewType.FEEDBACK){
  val isAudioPlaying = ObservableField<Boolean>(false)

  fun updateIsAudioPlaying(isPlaying: Boolean){
    isAudioPlaying.set(isPlaying)
  }
}
