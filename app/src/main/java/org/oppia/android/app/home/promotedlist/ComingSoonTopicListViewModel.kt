package org.oppia.android.app.home.promotedlist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel

/** [ViewModel] for the upcoming topic list displayed in [HomeFragment]. */
class ComingSoonTopicListViewModel(
  private val activity: AppCompatActivity,
  val comingSoonTopicsList: List<ComingSoonTopicsViewModel>
) : HomeItemViewModel() {

  /**
   * Returns the padding placed at the start of the coming soon topics list.
   */
  fun getStartPadding(): Int = activity.resources.getDimensionPixelSize(R.dimen.home_padding_start)

  /**
   * Returns the padding placed at the end of the promoted stories list based on the number of coming soon topics.
   */
  fun getEndPadding(): Int {
    return if (comingSoonTopicsList.size > 1) {
      activity.resources.getDimensionPixelSize(R.dimen.home_padding_end)
    } else {
      getStartPadding()
    }
  }
}
