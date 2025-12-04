package org.oppia.android.app.activity

import android.content.Intent
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams

// TODO(#59): Split this up into separate interfaces & move them to the corresponding activities.
//  This pattern will probably need to be used for all activities (& maybe fragments) as part of app
//  layer Bazel modularization.

/** Container of factories for creating launchable [Intent]s. */
interface ActivityIntentFactories {
  /**
   * Factory for starting new instances of topic activity.
   *
   * This must be injected within an activity context.
   */
  interface TopicActivityIntentFactory {
    /**
     * Returns a new [Intent] to start the topic activity for the specified profile, classroom
     * and topic.
     */
    fun createIntent(profileId: ProfileId, classroomId: String, topicId: String): Intent

    /**
     * Returns a new [Intent] to start the topic activity for the specified profile, classroom,
     * topic, and story (where the activity will automatically navigate to & expand the specified
     * story in the topic).
     */
    fun createIntent(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String
    ): Intent
  }

  /**
   * Factory for starting new instances of recently played activity.
   *
   * This must be injected within an activity context.
   */
  interface RecentlyPlayedActivityIntentFactory {
    /** Returns a new [Intent] to start the recently played activity for the specified profile. */
    fun createIntent(recentlyPlayedActivityParams: RecentlyPlayedActivityParams): Intent
  }
}
