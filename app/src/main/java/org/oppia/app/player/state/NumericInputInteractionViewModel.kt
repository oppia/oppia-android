package org.oppia.app.player.state

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class NumericInputInteractionViewModel @Inject constructor() : ViewModel() {
  var placeholder = ""
}
