package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for Mark Topics Completed. */
class MarkTopicsCompletedActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var markTopicsCompletedActivityPresenter: MarkTopicsCompletedActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(MARK_TOPICS_COMPLETED_ACTIVITY_PROFILE_ID_KEY, -1)
    markTopicsCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = getString(R.string.mark_topics_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val MARK_TOPICS_COMPLETED_ACTIVITY_PROFILE_ID_KEY =
      "MarkTopicsCompletedActivity.internal_profile_id"

    fun createMarkTopicsCompletedIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkTopicsCompletedActivity::class.java)
      intent.putExtra(MARK_TOPICS_COMPLETED_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
