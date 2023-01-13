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

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_chapters_completed_activity)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    if (getMarkChaptersCompletedFragment() == null) {
      val markChaptersCompletedFragment =
        MarkChaptersCompletedFragment.newInstance(internalProfileId, showConfirmationNotice = false)
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
    const val PROFILE_ID_EXTRA_KEY = "MarkChaptersCompletedTestActivity.profile_id"

    /** Returns an [Intent] for [MarkChaptersCompletedTestActivity]. */
    fun createMarkChaptersCompletedTestIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkChaptersCompletedTestActivity::class.java)
      intent.putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
      return intent
    }
  }
}
