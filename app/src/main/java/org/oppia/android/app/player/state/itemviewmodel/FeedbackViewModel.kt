package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/** [StateItemViewModel] for feedback blurbs. */
class FeedbackViewModel(
  val contentId: String,
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean
) : StateItemViewModel(ViewType.FEEDBACK) {
  val isPlaying = ObservableField<Boolean>(false)
  val isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)

  fun updateIsAudioPlaying(playing: Boolean) = isPlaying.set(playing)

  fun updateIsInitialized(initialized: Boolean) {
    isInitialized.value = initialized
  }
}
