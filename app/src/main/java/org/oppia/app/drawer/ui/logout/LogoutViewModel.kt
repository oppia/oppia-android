package org.oppia.app.drawer.ui.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogoutViewModel : ViewModel() {

  private val _text = MutableLiveData<String>().apply {
    value = "This is Logout Fragment"
  }
  val text: LiveData<String> = _text
}
