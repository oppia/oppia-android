package org.oppia.app.help

import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the recycler view of HelpActivity. */
class HelpViewModel @Inject constructor() : ObservableViewModel() {
  public var title = ""

  constructor(category: String) : this() {
    this.title = category
  }
}
