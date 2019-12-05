package org.oppia.app.player.exploration

import androidx.databinding.ObservableField
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

@ActivityScope
class ExplorationViewModel @Inject constructor(): ObservableViewModel() {
  /** Used to control visibility of audio button. */
  val showAudioButton = ObservableField(false)
  /** Used to change audio button src. */
  val isAudioStreamingOn = ObservableField(false)
}