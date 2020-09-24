package org.oppia.android.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel : HomeItemViewModel() {
  val profileName = ObservableField<String>("")
  val greeting = ObservableField<String>("")
}
