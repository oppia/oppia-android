package org.oppia.android.app.walkthrough.topiclist.topiclistviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.oppia.android.app.walkthrough.topiclist.WalkthroughTopicItemViewModel

/** [ViewModel] What do want to learn text in [WalkthroughTopicListFragment]. */
class WalkthroughTopicHeaderViewModel : WalkthroughTopicItemViewModel() {
  private val isHeaderTextVisibleMutableLiveData = MutableLiveData<Boolean>(true)
  val isHeaderTextVisible: LiveData<Boolean> = isHeaderTextVisibleMutableLiveData

  fun changeHeaderTextVisibility(visibility: Boolean) {
    isHeaderTextVisibleMutableLiveData.value = visibility
  }
}
