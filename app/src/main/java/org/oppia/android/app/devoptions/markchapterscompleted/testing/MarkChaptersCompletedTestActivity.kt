package org.oppia.android.app.devoptions.markchapterscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** The activity for testing [MarkChaptersCompletedFragment]. */
class MarkChaptersCompletedTestActivity : InjectableAppCompatActivity() {

  private lateinit var profileId: ProfileId

class MarkChaptersCompletedTestActivity : InjectableSystemLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_chapters_completed_activity)
    profileId = intent.extractCurrentUserProfileId()
    val showConfirmationNotice =
      intent.getBooleanExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, /* default= */ false)
    if (getMarkChaptersCompletedFragment() == null) {
      val markChaptersCompletedFragment = MarkChaptersCompletedFragment
        .newInstance(profileId, showConfirmationNotice)
      supportFragmentManager.beginTransaction().add(
        R.id.mark_chapters_completed_container,
        markChaptersCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkChaptersCompletedFragment(): MarkChaptersCompletedFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.mark_chapters_completed_container) as MarkChaptersCompletedFragment?
  }

  companion object {
    private const val SHOW_CONFIRMATION_NOTICE_EXTRA_KEY =
      "MarkChaptersCompletedTestActivity.show_confirmation_notice"

    /** Returns an [Intent] for [MarkChaptersCompletedTestActivity]. */
    fun createMarkChaptersCompletedTestIntent(
      context: Context,
      profileId: ProfileId,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedTestActivity::class.java)
      intent.decorateWithUserProfileId(profileId)
      intent.putExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, showConfirmationNotice)
      return intent
    }
  }
}
