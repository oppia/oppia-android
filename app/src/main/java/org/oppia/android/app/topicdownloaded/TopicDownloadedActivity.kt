package org.oppia.android.app.topicdownloaded

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.topic.TopicActivity
import javax.inject.Inject

private const val TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY =
  "TopicDownloadedActivity.topic_id"
private const val TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY =
  "TopicDownloadedActivity.internal_profile_id"

/** The activity for displaying [TopicDownloadedFragment]. */
class TopicDownloadedActivity : InjectableAppCompatActivity(), RouteToTopicListener {

  companion object {
    /** Returns the [TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY] key. */
    fun getProfileIdKey(): String {
      return TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY
    }

    /** Returns the [TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY] key. */
    fun getTopicIdKey(): String {
      return TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
    }

    /**
     * This function creates the intent of [TopicDownloadedActivity].
     *
     * @param [context]: context of the screen from where thi activity starts.
     * @param [internalProfileId]: Id of the profile.
     * @param [topicId]: Id of the topic.
     *
     * @return [Intent]: TopicDownloadedActivity Intent object.
     */
    fun createTopicDownloadedActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String
    ): Intent {
      val intent = Intent(context, TopicDownloadedActivity::class.java)
      intent.putExtra(TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      intent.putExtra(TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      return intent
    }
  }

  @Inject
  lateinit var topicDownloadedActivityPresenter: TopicDownloadedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val topicId =
      checkNotNull(intent.getStringExtra(TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
        "Expected extra topic ID to be included for TopicDownloadedActivity."
      }
    val internalProfileId =
      intent.getIntExtra(TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY, -1)
    topicDownloadedActivityPresenter.handleOnCreate(internalProfileId, topicId)
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    startActivity(
      TopicActivity.createTopicActivityIntent(
        this,
        internalProfileId,
        topicId
      )
    )
    finish()
  }
}
