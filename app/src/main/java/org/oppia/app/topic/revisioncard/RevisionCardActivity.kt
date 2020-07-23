package org.oppia.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for revision card. */
class RevisionCardActivity : InjectableAppCompatActivity(), ReturnToTopicClickListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)

    val topicId = checkNotNull(intent?.getStringExtra(TOPIC_ID_EXTRA_KEY)) {
      "Expected topic ID to be included in intent for RevisionCardActivity."
    }
    val subtopicId = intent?.getIntExtra(SUBTOPIC_ID_EXTRA_KEY, -1)!!
    revisionCardActivityPresenter.handleOnCreate(topicId, subtopicId)
  }

  companion object {
    internal const val TOPIC_ID_EXTRA_KEY = "TOPIC_ID_EXTRA_KEY"
    internal const val SUBTOPIC_ID_EXTRA_KEY = "SUBTOPIC_ID_EXTRA_KEY"

    /** Returns a new [Intent] to route to [RevisionCardActivity]. */
    fun createRevisionCardActivityIntent(
      context: Context,
      topicId: String,
      subtopicId: Int
    ): Intent {
      val intent = Intent(context, RevisionCardActivity::class.java)
      intent.putExtra(TOPIC_ID_EXTRA_KEY, topicId)
      intent.putExtra(SUBTOPIC_ID_EXTRA_KEY, subtopicId)
      return intent
    }
  }

  override fun onReturnToTopicClicked() {
    onBackPressed()
  }
}
