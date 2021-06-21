package org.oppia.android.app.devoptions.markstoriescompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for Mark Stories Completed. */
class MarkStoriesCompletedActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var markStoriesCompletedActivityPresenter: MarkStoriesCompletedActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(MARK_STORIES_COMPLETED_ACTIVITY_PROFILE_ID_KEY, -1)
    markStoriesCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = getString(R.string.developer_options_mark_stories_completed)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val MARK_STORIES_COMPLETED_ACTIVITY_PROFILE_ID_KEY =
      "MarkStoriesCompletedActivity.internal_profile_id"

    fun createMarkStoriesCompletedIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkStoriesCompletedActivity::class.java)
      intent.putExtra(MARK_STORIES_COMPLETED_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
