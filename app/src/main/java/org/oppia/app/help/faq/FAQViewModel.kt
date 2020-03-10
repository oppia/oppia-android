package org.oppia.app.help.faq

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the recycler view of FAQActivity. */
class FAQViewModel @Inject constructor() : ObservableViewModel() {
  public var title: String = ""

  constructor(title: String) : this() {
    this.title = title
  }

  /** Used to control visibility of divider. */
  public var showDivider = ObservableField(true)
}
