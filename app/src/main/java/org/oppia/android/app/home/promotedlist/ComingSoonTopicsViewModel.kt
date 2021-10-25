package org.oppia.android.app.home.promotedlist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.UpcomingTopic

/** [ViewModel] for displaying a coming soon topic summaries. */
class ComingSoonTopicsViewModel(
  private val activity: AppCompatActivity,
  val topicSummary: UpcomingTopic,
  val entityType: String,
  val comingSoonTopicList: ComingSoonTopicList
) : HomeItemViewModel() {
  val name: String = topicSummary.name

  /**
   * Returns the padding placed at the start of the coming soon topics list.
   */
  private fun getStartPadding(): Int =
    activity.resources.getDimensionPixelSize(R.dimen.coming_soon_padding_start)

  /**
   * Returns the padding placed at the end of the coming soon topics list based on the number of coming soon topics.
   */
  fun getEndMargin(): Int {
    return if (comingSoonTopicList.upcomingTopicCount > 2) {
      activity.resources.getDimensionPixelSize(R.dimen.coming_soon_padding_end)
    } else {
      getStartPadding()
    }
  }
}
