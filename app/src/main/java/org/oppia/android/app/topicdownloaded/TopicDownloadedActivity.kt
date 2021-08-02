package org.oppia.android.app.topicdownloaded

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for displaying [TopicDownloadedFragment]. */
class TopicDownloadedActivity : InjectableAppCompatActivity() {

  companion object {

    fun createTopicDownloadedActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String
    ): Intent {
      val intent = Intent(context, TopicDownloadedActivity::class.java)
      intent.putExtra("id", internalProfileId)
      intent.putExtra("topicId", topicId)
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
    topicDownloadedActivityPresenter.handleOnCreate(internalProfileId, topicId)
  }
}
