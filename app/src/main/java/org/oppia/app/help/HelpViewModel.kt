package org.oppia.app.help

import androidx.lifecycle.ViewModel
import javax.inject.Inject

/** [ViewModel] for the recycler view of HelpActivity. */
class HelpViewModel @Inject constructor(
) : ViewModel(){
  var title = ""
  constructor(category: String) : this() {
    this.title = category
  }
}
