package org.oppia.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val TOPIC_ID_ARGUMENT_KEY = "TOPIC_ID_"
const val SUBTOPIC_ID_ARGUMENT_KEY = "SUBTOPIC_ID"

/** Activity for revision card. */
class RevisionCardActivity : InjectableAppCompatActivity(), ReturnToTopicClickListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    revisionCardActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [RevisionCardActivity]. */
    fun createRevisionCardActivityIntent(
      context: Context,
      topicId: String,
      subtopicId: String
    ): Intent {
      val intent = Intent(context, RevisionCardActivity::class.java)
      intent.putExtra(TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(SUBTOPIC_ID_ARGUMENT_KEY, subtopicId)
      return intent
    }
  }

  override fun onReturnToTopicClicked() {
    onBackPressed()
  }
}
