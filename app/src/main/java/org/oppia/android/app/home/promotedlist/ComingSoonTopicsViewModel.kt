package org.oppia.android.app.home.promotedlist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.UpcomingTopic

// TODO(#206): Remove the color darkening computation and properly set up the topic thumbnails.
// These values were roughly computed based on the mocks. They won't produce the same colors since darker colors in the
// mocks were not consistently darker. An alternative would be to specify both background colors together to ensure
// proper contrast with readable elements.
/** The view model corresponding to coming soon topic summaries in the topic summary RecyclerView. */
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
  fun getStartPadding(): Int =
    activity.resources.getDimensionPixelSize(R.dimen.coming_soon_padding_start)

  /**
   * Returns the padding placed at the end of the coming soon topics list based on the number of coming soon topics.
   */
  fun getEndPadding(): Int {
    return if (comingSoonTopicList.upcomingTopicCount > 2) {
      activity.resources.getDimensionPixelSize(R.dimen.coming_soon_padding_end)
    } else {
      getStartPadding()
    }
  }
}
