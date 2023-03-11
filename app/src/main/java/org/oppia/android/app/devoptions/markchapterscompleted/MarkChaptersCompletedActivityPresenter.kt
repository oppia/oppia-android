package org.oppia.android.app.devoptions.markchapterscompleted

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [MarkChaptersCompletedActivity]. */
@ActivityScope
class MarkChaptersCompletedActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate(internalProfileId: Int, showConfirmationNotice: Boolean) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.mark_chapters_completed_activity)

    if (getMarkChaptersCompletedFragment() == null) {
      val markChaptersCompletedFragment =
        MarkChaptersCompletedFragment.newInstance(internalProfileId, showConfirmationNotice)
      activity.supportFragmentManager.beginTransaction().add(
        R.id.mark_chapters_completed_container,
        markChaptersCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkChaptersCompletedFragment(): MarkChaptersCompletedFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.mark_chapters_completed_container) as MarkChaptersCompletedFragment?
  }
}
