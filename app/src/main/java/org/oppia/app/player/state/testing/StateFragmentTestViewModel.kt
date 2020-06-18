package org.oppia.app.player.state.testing

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.activity.ActivityScope
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [StateFragmentTestActivity]. */
@ActivityScope
class StateFragmentTestViewModel @Inject constructor() : ObservableViewModel() {
  val hasExplorationStarted = ObservableField<Boolean>(false)
}
