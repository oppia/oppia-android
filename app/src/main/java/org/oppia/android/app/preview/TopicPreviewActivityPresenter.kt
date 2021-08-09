package org.oppia.android.app.preview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** Profile id argument key helps in passing to TopicPreviewFragment. */
const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
/** Topic id argument key help in passing to TopicPreviewFragment. */
const val TOPIC_ID_ARGUMENT_KEY = "topic_id"

/** The presenter for [TopicPreviewActivity]. */
@ActivityScope
class TopicPreviewActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Shows the TopicPreviewFragment */
  fun handleOnCreate(internalProfileId: Int, topicId: String) {
    activity.setContentView(R.layout.topic_preview_activity)
    // Bundle object to pass to TopicPreviewFragment.
    val args = Bundle()
    args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
    args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
    val topicPreviewFragment = TopicPreviewFragment()
    topicPreviewFragment.arguments = args
    activity.supportFragmentManager.beginTransaction().add(
      R.id.topic_preivew_fragment_placeholder,
      topicPreviewFragment, TOPIC_PREVIEW_FRAGMENT_TAG
    ).commitNow()
  }
}
