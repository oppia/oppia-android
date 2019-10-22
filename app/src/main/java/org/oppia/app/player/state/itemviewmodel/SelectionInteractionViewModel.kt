package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for multiple or item-selection input choice list. */
@FragmentScope
class SelectionInteractionViewModel @Inject constructor() : ViewModel() {

  var choiceItems: MutableList<String>? = null
  var interactionId: String =""
}
