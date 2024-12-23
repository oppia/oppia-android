package org.oppia.android.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController

/** Recently played item [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class RecentlyPlayedStorySummaryViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val promotedStory: PromotedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val resourceHandler: AppLanguageResourceHandler,
  translationController: TranslationController
) : ProfileProgressItemViewModel(), RouteToTopicPlayStoryListener {
  val storyTitle by lazy {
    translationController.extractString(
      promotedStory.storyTitle, promotedStory.storyWrittenTranslationContext
    )
  }
  val topicTitle by lazy {
    translationController.extractString(
      promotedStory.topicTitle, promotedStory.topicWrittenTranslationContext
    )
  }
  val nextChapterTitle by lazy {
    translationController.extractString(
      promotedStory.nextChapterTitle, promotedStory.nextChapterWrittenTranslationContext
    )
  }

  fun onStoryItemClicked() {
    routeToTopicPlayStory(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      classroomId = promotedStory.classroomId,
      topicId = promotedStory.topicId,
      storyId = promotedStory.storyId
    )
  }

  fun computeLessonThumbnailContentDescription(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.lesson_thumbnail_content_description, nextChapterTitle
    )
  }

  override fun routeToTopicPlayStory(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {
    val intent = intentFactoryShim.createTopicPlayStoryActivityIntent(
      activity.applicationContext,
      profileId.internalId,
      classroomId,
      topicId,
      storyId
    )
    activity.startActivity(intent)
  }
}
