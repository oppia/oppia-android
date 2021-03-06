package org.oppia.android.app.shim

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.topic.TopicActivity
import javax.inject.Inject

/**
 * Creates intents for ViewModels in order to avoid ViewModel files directly depending on Activites.
 * When working on a ViewModel file, developers should refrain from directly referencing Activities
 * by adding all Intent functionality here.
 *
 * Please note that this file is temporary and all functionality will be returned to its respective
 * ViewModel once Gradle has been removed.
 */
// TODO(#1619): Remove file post-Gradle
class IntentFactoryShimImpl @Inject constructor() : IntentFactoryShim {

  private val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
  private val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

  /** Returns [ProfileChooserActivity] intent for [AdministratorControlsAccountActionsViewModel]. */
  override fun createProfileChooserActivityIntent(fragment: FragmentActivity): Intent {
    return Intent(fragment, ProfileChooserActivity::class.java)
  }

  /**
   * Creates a [TopicActivity] intent for [PromotedStoryViewModel] and passes necessary string
   * data.
   * */
  override fun createTopicPlayStoryActivityIntent(
    context: Context,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent {
    val intent = Intent(context, TopicActivity::class.java)
    intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
    intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
    intent.putExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
    return intent
  }

  /**
   * Creates a [TopicActivity] intent which opens info-tab.
   * */
  override fun createTopicActivityIntent(
    context: Context,
    internalProfileId: Int,
    topicId: String
  ): Intent {
    val intent = Intent(context, TopicActivity::class.java)
    intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
    intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
    return intent
  }

  /**
   * Creates a [RecentlyPlayedActivity] intent for [PromotedStoryListViewModel] and passes
   * necessary string data.
   * */
  override fun createRecentlyPlayedActivityIntent(
    context: Context,
    internalProfileId: Int
  ): Intent {
    val intent = Intent(context, RecentlyPlayedActivity::class.java)
    intent.putExtra(
      RecentlyPlayedActivity.RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
      internalProfileId
    )
    return intent
  }
}
