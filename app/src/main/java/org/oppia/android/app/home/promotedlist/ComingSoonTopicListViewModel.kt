package org.oppia.android.app.home.promotedlist

import androidx.lifecycle.ViewModel
import org.oppia.android.app.home.HomeItemViewModel
import java.util.Objects

/** [ViewModel] for the upcoming topic list displayed in [HomeFragment]. */
class ComingSoonTopicListViewModel(
  val comingSoonTopicList: List<ComingSoonTopicsViewModel>
) : HomeItemViewModel() {
  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is ComingSoonTopicListViewModel &&
      other.comingSoonTopicList == this.comingSoonTopicList
  }

  override fun hashCode() = Objects.hash(comingSoonTopicList)
}
