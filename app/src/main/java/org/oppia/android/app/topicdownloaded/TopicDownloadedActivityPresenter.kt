package org.oppia.android.app.topicdownloaded

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.topic.TOPIC_FRAGMENT_TAG
import javax.inject.Inject

const val TOPIC_DOWNLOADED_FRAGMENT_TAG = "TopicDownloadedFragment"

/** The presenter for [TopicDownloadedActivity]. */
@ActivityScope
class TopicDownloadedActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate(internalProfileId: Int, topicId: String) {
    activity.setContentView(R.layout.topic_downloaded_activity)
    val args = Bundle()
    args.putInt("id", internalProfileId)
    args.putString("topicId", topicId)
    val topicDownloadedFragment = TopicDownloadedFragment()
    topicDownloadedFragment.arguments = args
    activity.supportFragmentManager.beginTransaction().add(
      R.id.topic_downloaded_fragment_placeholder,
      topicDownloadedFragment, TOPIC_FRAGMENT_TAG
    ).commitNow()
  }
}
