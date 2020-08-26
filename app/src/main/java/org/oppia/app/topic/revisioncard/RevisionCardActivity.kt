package org.oppia.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for revision card. */
class RevisionCardActivity : InjectableAppCompatActivity(), ReturnToTopicClickListener {

  @Inject
  lateinit var revisionCardActivityPresenter: RevisionCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    intent?.let { intent ->
      val internalProfileId = intent.getIntExtra(INTERNAL_PROFILE_ID_EXTRA_KEY, -1)
      val topicId = checkNotNull(intent.getStringExtra(TOPIC_ID_EXTRA_KEY)) {
        "Expected topic ID to be included in intent for RevisionCardActivity."
      }
      val subtopicId = intent.getIntExtra(SUBTOPIC_ID_EXTRA_KEY, -1)
      revisionCardActivityPresenter.handleOnCreate(internalProfileId, topicId, subtopicId)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_reading_options, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return revisionCardActivityPresenter.handleOnOptionsItemSelected(item)
  }

  companion object {
    internal const val INTERNAL_PROFILE_ID_EXTRA_KEY = "RevisionCardActivity.internal_profile_id"
    internal const val TOPIC_ID_EXTRA_KEY = "RevisionCardActivity.topic_id"
    internal const val SUBTOPIC_ID_EXTRA_KEY = "RevisionCardActivity.subtopic_id"

    /** Returns a new [Intent] to route to [RevisionCardActivity]. */
    fun createRevisionCardActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      subtopicId: Int
    ): Intent {
      val intent = Intent(context, RevisionCardActivity::class.java)
      intent.putExtra(INTERNAL_PROFILE_ID_EXTRA_KEY, internalProfileId)
      intent.putExtra(TOPIC_ID_EXTRA_KEY, topicId)
      intent.putExtra(SUBTOPIC_ID_EXTRA_KEY, subtopicId)
      return intent
    }
  }

  override fun onReturnToTopicClicked() {
    onBackPressed()
  }
}
