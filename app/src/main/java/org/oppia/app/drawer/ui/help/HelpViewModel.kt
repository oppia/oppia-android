package org.oppia.app.drawer.ui.help

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HelpViewModel : ViewModel() {

  private val _text = MutableLiveData<String>().apply {
    value = "This is help Fragment"
  }
  val text: LiveData<String> = _text
}