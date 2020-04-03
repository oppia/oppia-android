package org.oppia.app.home

import androidx.lifecycle.ViewModel

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel : HomeItemViewModel() {
  var profileName : String = ""
  var greeting : String = ""
}
