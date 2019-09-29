package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }
}
