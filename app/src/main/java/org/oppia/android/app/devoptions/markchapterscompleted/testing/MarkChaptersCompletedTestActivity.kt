package org.oppia.android.app.devoptions.markchapterscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment

/** The activity for testing [MarkChaptersCompletedFragment]. */
class MarkChaptersCompletedTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_chapters_completed_activity)
    val internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, /* default= */ -1)
    val showConfirmationNotice =
      intent.getBooleanExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, /* default= */ false)
    if (getMarkChaptersCompletedFragment() == null) {
      val markChaptersCompletedFragment =
        MarkChaptersCompletedFragment.newInstance(internalProfileId, showConfirmationNotice)
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
    private const val PROFILE_ID_EXTRA_KEY = "MarkChaptersCompletedTestActivity.profile_id"
    private const val SHOW_CONFIRMATION_NOTICE_EXTRA_KEY =
      "MarkChaptersCompletedTestActivity.show_confirmation_notice"

    /** Returns an [Intent] for [MarkChaptersCompletedTestActivity]. */
    fun createMarkChaptersCompletedTestIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedTestActivity::class.java)
      intent.putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
      intent.putExtra(SHOW_CONFIRMATION_NOTICE_EXTRA_KEY, showConfirmationNotice)
      return intent
    }
  }
}
