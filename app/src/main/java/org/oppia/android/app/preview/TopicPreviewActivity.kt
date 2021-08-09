package org.oppia.android.app.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val TOPIC_PREVIEW_FRAGMENT_TAG = "TopicPreviewFragment"
private const val TOPIC_PREVIEW_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicPreviewActivity.topic_id"
private const val TOPIC_PREVIEW_ACTIVITY_PROFILE_ID_ARGUMENT_KEY = "TopicPreviewActivity.profile_id"

class TopicPreviewActivity : InjectableAppCompatActivity() {

  companion object {
    /**
     * This function creates the intent of [TopicPreviewActivity].
     *
     * @param [internalProfileId]: Id of the Profile.
     * @param [topicId]: Id of the Topic.
     *
     * @return [Intent]: TopicPreviewActivity Intent object.
     */
    fun createTopicPreviewActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String
    ): Intent {
      val intent = Intent(context, TopicPreviewActivity::class.java)
      intent.putExtra(TOPIC_PREVIEW_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      intent.putExtra(TOPIC_PREVIEW_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      return intent
    }
  }

  @Inject
  lateinit var topicPreviewActivityPresenter: TopicPreviewActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId =
      intent?.getIntExtra(TOPIC_PREVIEW_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId =
      checkNotNull(intent?.getStringExtra(TOPIC_PREVIEW_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topic ID to be included in intent for TopicPreviewActivity."
      }
    topicPreviewActivityPresenter.handleOnCreate(internalProfileId, topicId)
  }
}
