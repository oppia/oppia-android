package org.oppia.android.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.CompletedStory
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController

/** Completed story view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val completedStory: CompletedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  translationController: TranslationController
) : ObservableViewModel(), RouteToTopicPlayStoryListener {
  /** Holds lazily loaded completedStoryName [String] value. */
  val completedStoryName by lazy {
    translationController.extractString(
      completedStory.storyTitle, completedStory.storyWrittenTranslationContext
    )
  }
  /** Holds lazily loaded topicName [String] value. */
  val topicName by lazy {
    translationController.extractString(
      completedStory.topicTitle, completedStory.topicWrittenTranslationContext
    )
  }

  /** Called when user clicks on CompletedStoryItem. */
  fun onCompletedStoryItemClicked() {
    routeToTopicPlayStory(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      completedStory.classroomId,
      completedStory.topicId,
      completedStory.storyId
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
