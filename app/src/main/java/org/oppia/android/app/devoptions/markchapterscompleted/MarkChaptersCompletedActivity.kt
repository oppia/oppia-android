package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.MARK_CHAPTERS_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Mark Chapters Completed. */
class MarkChaptersCompletedActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var markChaptersCompletedActivityPresenter: MarkChaptersCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    markChaptersCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.mark_chapters_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val PROFILE_ID_EXTRA_KEY = "MarkChaptersCompletedActivity.profile_id"

    fun createMarkChaptersCompletedIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, MarkChaptersCompletedActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
        decorateWithScreenName(MARK_CHAPTERS_COMPLETED_ACTIVITY)
      }
    }
  }
}
