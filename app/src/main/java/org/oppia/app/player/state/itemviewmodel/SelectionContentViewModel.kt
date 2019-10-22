package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class SelectionContentViewModel @Inject constructor() : ViewModel() {
  var htmlContent: String =""
}
