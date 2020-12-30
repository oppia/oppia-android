package org.oppia.android.app.home.promotedlist

import androidx.annotation.ColorInt
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.UpcomingTopic

// TODO(#206): Remove the color darkening computation and properly set up the topic thumbnails.
// These values were roughly computed based on the mocks. They won't produce the same colors since darker colors in the
// mocks were not consistently darker. An alternative would be to specify both background colors together to ensure
// proper contrast with readable elements.

/** The view model corresponding to coming soon topic summaries in the topic summary RecyclerView. */
class ComingSoonTopicsViewModel(
  val topicSummary: UpcomingTopic,
  val entityType: String
) : HomeItemViewModel() {
  val name: String = topicSummary.name

  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor()

  @ColorInt
  private fun retrieveBackgroundColor(): Int {
    return topicSummary.lessonThumbnail.backgroundColorRgb
  }
}
