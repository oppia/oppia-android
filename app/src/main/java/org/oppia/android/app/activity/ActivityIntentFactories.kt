package org.oppia.android.app.activity

import android.content.Intent
import org.oppia.android.app.model.ProfileId

// TODO(#59): Split this up into separate interfaces & move them to the corresponding activities.
//  This pattern will probably need to be used for all activities (& maybe fragments) as part of app
//  layer Bazel modularization.

// TODO: document that each of these must be injected within an activity context.
interface ActivityIntentFactories {
  interface TopicActivityIntentFactory {
    fun createIntent(profileId: ProfileId, topicId: String): Intent
    fun createIntent(profileId: ProfileId, topicId: String, storyId: String): Intent
  }

  interface RecentlyPlayedActivityIntentFactory {
    fun createIntent(profileId: ProfileId): Intent
  }
}
