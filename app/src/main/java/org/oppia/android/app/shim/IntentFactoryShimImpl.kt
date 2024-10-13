package org.oppia.android.app.shim

import android.content.Context
import android.content.Intent
import org.oppia.android.app.activity.ActivityIntentFactories.RecentlyPlayedActivityIntentFactory
import org.oppia.android.app.activity.ActivityIntentFactories.TopicActivityIntentFactory
import org.oppia.android.app.model.ProfileId
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
class IntentFactoryShimImpl @Inject constructor(
  private val topicActivityIntentFactory: TopicActivityIntentFactory,
  private val recentlyPlayedActivityIntentFactory: RecentlyPlayedActivityIntentFactory
) : IntentFactoryShim {

  /**
   * Creates a topic activity intent for [PromotedStoryViewModel] and passes necessary string
   * data.
   */
  override fun createTopicPlayStoryActivityIntent(
    context: Context,
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String
  ): Intent {
    return topicActivityIntentFactory.createIntent(
      ProfileId.newBuilder().apply {
        loggedInInternalProfileId = internalProfileId
      }.build(),
      classroomId,
      topicId,
      storyId
    )
  }

  /**
   * Creates a topic activity intent which opens info-tab.
   */
  override fun createTopicActivityIntent(
    context: Context,
    internalProfileId: Int,
    classroomId: String,
    topicId: String
  ): Intent {
    return topicActivityIntentFactory.createIntent(
      ProfileId.newBuilder().apply {
        loggedInInternalProfileId = internalProfileId
      }.build(),
      classroomId,
      topicId
    )
  }
}
