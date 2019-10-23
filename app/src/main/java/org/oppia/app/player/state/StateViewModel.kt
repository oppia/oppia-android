package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ObservableViewModel() {
  var stateName = ObservableField<String>()

  fun setStateName(state: String) {
    stateName.set(state)
  }
}
