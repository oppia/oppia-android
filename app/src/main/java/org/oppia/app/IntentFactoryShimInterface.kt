package org.oppia.app

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

interface IntentFactoryShimInterface {

    fun createProfileActivityIntent(fragment: Fragment): Intent

    fun createTopicActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): Intent
}