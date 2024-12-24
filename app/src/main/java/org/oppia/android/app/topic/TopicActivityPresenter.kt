package org.oppia.android.app.topic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicFragmentArguments
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

const val TOPIC_FRAGMENT_TAG = "TopicFragment"
const val TOPIC_FRAGMENT_ARGUMENTS_KEY = "TopicFragment.arguments"

/** The presenter for [TopicActivity]. */
@ActivityScope
class TopicActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var classroomId: String
  private lateinit var topicId: String

  fun handleOnCreate(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String?
  ) {
    this.topicId = topicId
    this.classroomId = classroomId
    activity.setContentView(R.layout.topic_activity)

    if (getTopicFragment() == null) {
      val topicFragment = TopicFragment()
      val arguments = Bundle().apply {
        val args = TopicFragmentArguments.newBuilder().apply {
          this.classroomId = classroomId
          this.topicId = topicId
          if (storyId != null) {
            this.storyId = storyId
          }
        }.build()
        putProto(TOPIC_FRAGMENT_ARGUMENTS_KEY, args)
        decorateWithUserProfileId(profileId)
      }

      topicFragment.arguments = arguments
      activity.supportFragmentManager.beginTransaction().add(
        R.id.topic_fragment_placeholder,
        topicFragment, TOPIC_FRAGMENT_TAG
      ).commitNow()
    }

    if (getSpotlightFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.topic_spotlight_fragment_placeholder,
        SpotlightFragment.newInstance(profileId.internalId),
        SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
  }

  private fun getTopicFragment(): TopicFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.topic_fragment_placeholder
      ) as TopicFragment?
  }

  private fun getSpotlightFragment(): SpotlightFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightFragment
  }
}
