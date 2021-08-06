package org.oppia.android.app.topicdownloaded

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val TOPIC_DOWNLOADED_ACTIVITY_TOPIC_ID_ARGUMENT_KEY =
  "TopicDownloadedActivity.topic_id"
private const val TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY =
  "TopicDownloadedActivity.internal_profile_id"

/** The activity for displaying [TopicDownloadedFragment]. */
class TopicDownloadedActivity : InjectableAppCompatActivity() {

  companion object {
    /**
     * This function creates the intent of [TopicDownloadedActivity].
     *
     * @param [content]: context of the screen from where thi activity starts.
     * @param [internalProfileId]: Id of the profile.
     * @param [topicId]: Id of the topic.
     *
     * @return [Intent]: Intent
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
        "Expected extra topic ID to be included for TopicDownloadedFragment."
      }
    val internalProfileId =
      intent.getIntExtra(TOPIC_DOWNLOADED_ACTIVITY_INTERNAL_PROFILE_ID_ARGUMENT_KEY, 0)
    topicDownloadedActivityPresenter.handleOnCreate(internalProfileId, topicId)
  }
}
