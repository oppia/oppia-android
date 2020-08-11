package org.oppia.app.shim

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity

/**
 * Creates intents for view models in order to avoid view model files depending on activites.
 */
interface IntentFactoryShim {

  fun createProfileActivityIntent(fragment: FragmentActivity): Intent

  fun createTopicPlayStoryActivityIntent(
    context: Context,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): Intent

  fun createRecentlyPlayedActivityIntent(context: Context, internalProfileId: Int): Intent
}
