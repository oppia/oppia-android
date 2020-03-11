package org.oppia.app.help.faq

import androidx.databinding.ObservableField

/** Content view model for the recycler view in [FAQFragment]. */
class FAQContentViewModel(val title: String) : FAQItemViewModel() {

  /** Used to control visibility of divider. */
  var showDivider = ObservableField(true)
}
