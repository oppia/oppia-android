package org.oppia.android.app.topicdownloaded

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

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
      topicId: String,
      topicName: String
    ): Intent {
      val intent = Intent(context, TopicDownloadedActivity::class.java)
      intent.putExtra("id", internalProfileId)
      intent.putExtra("topicId", topicId)
      intent.putExtra("topicName", topicName)
      return intent
    }
  }

  @Inject
  lateinit var topicDownloadedActivityPresenter: TopicDownloadedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val topicId = checkNotNull(intent.getStringExtra("topicId")) {
      "Expected extra topic ID to be included for TopicDownloadedFragment."
    }
    val internalProfileId = intent.getIntExtra("id", 0)
    val topicName = checkNotNull(intent.getStringExtra("topicName")) {
      "Expected extra topic Name to be included for TopicDownloadedFragment."
    }
    topicDownloadedActivityPresenter.handleOnCreate(internalProfileId, topicId, topicName)
  }
}
