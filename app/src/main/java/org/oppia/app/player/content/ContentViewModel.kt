package org.oppia.app.player.content

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for content-card state. */
@FragmentScope
class ContentViewModel @Inject constructor() : ViewModel() {

  var contentId = ""
  var htmlContent = ""
}
