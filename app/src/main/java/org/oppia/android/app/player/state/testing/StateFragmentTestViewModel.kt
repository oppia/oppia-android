package org.oppia.android.app.player.state.testing

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [StateFragmentTestActivity]. */
@ActivityScope
class StateFragmentTestViewModel @Inject constructor() : ObservableViewModel() {
  val hasExplorationStarted = ObservableField<Boolean>(false)
}
