package org.oppia.app.drawer.ui.mydownloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyDownloadsViewModel : ViewModel() {

  private val _text = MutableLiveData<String>().apply {
    value = "This is My Downloads Fragment"
  }
  val text: LiveData<String> = _text
}
