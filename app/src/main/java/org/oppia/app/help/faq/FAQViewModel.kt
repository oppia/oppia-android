package org.oppia.app.help.faq

import androidx.databinding.ObservableField
import org.oppia.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the recycler view of FAQActivity. */
class FAQViewModel(val title: String) : ObservableViewModel() {

  /** Used to control visibility of divider. */
  var showDivider = ObservableField(true)
}
