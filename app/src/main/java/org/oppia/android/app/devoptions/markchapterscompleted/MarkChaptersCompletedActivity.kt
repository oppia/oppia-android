package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Activity for Mark Chapters Completed. */
class MarkChaptersCompletedActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var markChaptersCompletedActivityPresenter: MarkChaptersCompletedActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(MARK_CHAPTERS_COMPLETED_ACTIVITY_PROFILE_ID_KEY, -1)
    markChaptersCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = getString(R.string.mark_chapters_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val MARK_CHAPTERS_COMPLETED_ACTIVITY_PROFILE_ID_KEY =
      "MarkChaptersCompletedActivity.internal_profile_id"

    fun createMarkChaptersCompletedIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkChaptersCompletedActivity::class.java)
      intent.putExtra(MARK_CHAPTERS_COMPLETED_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}