package org.oppia.android.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Recently played item [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class RecentlyPlayedStorySummaryViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val promotedStory: PromotedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val resourceHandler: AppLanguageResourceHandler
) : ProfileProgressItemViewModel(), RouteToTopicPlayStoryListener {

  fun onStoryItemClicked() {
    routeToTopicPlayStory(internalProfileId, promotedStory.topicId, promotedStory.storyId)
  }

  fun computeLessonThumbnailContentDescription(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.lesson_thumbnail_content_description, promotedStory.nextChapterName
    )
  }

  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    val intent = intentFactoryShim.createTopicPlayStoryActivityIntent(
      activity.applicationContext,
      internalProfileId,
      topicId,
      storyId
    )
    activity.startActivity(intent)
  }
}
