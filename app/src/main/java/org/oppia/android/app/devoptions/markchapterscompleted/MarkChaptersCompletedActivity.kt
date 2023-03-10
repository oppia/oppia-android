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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, /* defaultValue= */ -1)
    val showConfirmationNotice =
      intent.getBooleanExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, /* defaultValue= */ false)
    markChaptersCompletedActivityPresenter.handleOnCreate(internalProfileId, showConfirmationNotice)
    title = resourceHandler.getStringInLocale(R.string.mark_chapters_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    private const val PROFILE_ID_EXTRA_KEY = "MarkChaptersCompletedActivity.profile_id"
    private const val SHOW_CONFIRMATION_NOTICE_EXTRA_KEY =
      "MarkChaptersCompletedActivity.show_confirmation_notice"

    fun createMarkChaptersCompletedIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      return Intent(context, MarkChaptersCompletedActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
        putExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, showConfirmationNotice)
        decorateWithScreenName(MARK_CHAPTERS_COMPLETED_ACTIVITY)
      }
    }
  }
}
