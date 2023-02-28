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
  private val profileId: ProfileId,
  val completedStory: CompletedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  translationController: TranslationController
) : ObservableViewModel(), RouteToTopicPlayStoryListener {
  val completedStoryName by lazy {
    translationController.extractString(
      completedStory.storyTitle, completedStory.storyWrittenTranslationContext
    )
  }
  val topicName by lazy {
    translationController.extractString(
      completedStory.topicTitle, completedStory.topicWrittenTranslationContext
    )
  }

  fun onCompletedStoryItemClicked() {
    routeToTopicPlayStory(profileId, completedStory.topicId, completedStory.storyId)
  }

  override fun routeToTopicPlayStory(profileId: ProfileId, topicId: String, storyId: String) {
    val intent = intentFactoryShim.createTopicPlayStoryActivityIntent(
      activity.applicationContext,
      profileId,
      topicId,
      storyId
    )
    activity.startActivity(intent)
  }
}
