package org.oppia.app.shim

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity

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
