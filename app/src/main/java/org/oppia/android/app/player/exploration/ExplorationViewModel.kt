package org.oppia.android.app.player.exploration

import androidx.databinding.ObservableField
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [ExplorationActivity] */
@ActivityScope
class ExplorationViewModel @Inject constructor() : ObservableViewModel() {
  /** Used to control visibility of audio button. */
  val showAudioButton = ObservableField(false)

  /** Used to change the audio button enabled status. */
  val isAudioStreamingOn = ObservableField(false)
}
