package org.oppia.app.player.state

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class InteractionReadOnlyViewModel @Inject constructor() : ViewModel() {

  var contentId = ""
  var htmlContent = ""
}
