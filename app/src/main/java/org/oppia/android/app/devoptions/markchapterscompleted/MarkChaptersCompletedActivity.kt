package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.MARK_CHAPTERS_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for Mark Chapters Completed. */
class MarkChaptersCompletedActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var markChaptersCompletedActivityPresenter: MarkChaptersCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    val showConfirmationNotice =
      intent.getBooleanExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, /* defaultValue= */ false)
    markChaptersCompletedActivityPresenter.handleOnCreate(profileId, showConfirmationNotice)
    title = resourceHandler.getStringInLocale(R.string.mark_chapters_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    private const val SHOW_CONFIRMATION_NOTICE_EXTRA_KEY =
      "MarkChaptersCompletedActivity.show_confirmation_notice"

    /** Returns an [Intent] to start this activity. */
    fun createMarkChaptersCompletedIntent(
      context: Context,
      profileId: ProfileId,
      showConfirmationNotice: Boolean
    ): Intent {
      return Intent(context, MarkChaptersCompletedActivity::class.java).apply {
        decorateWithScreenName(MARK_CHAPTERS_COMPLETED_ACTIVITY)
        decorateWithUserProfileId(profileId)
        putExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, showConfirmationNotice)
      }
    }
  }
}
