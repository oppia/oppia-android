package org.oppia.android.app.devoptions.marktopicscompleted

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [MarkTopicsCompletedActivity]. */
@ActivityScope
class MarkTopicsCompletedActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate(internalProfileId: Int) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.mark_topics_completed_activity)

    if (getMarkTopicsCompletedFragment() == null) {
      val markTopicsCompletedFragment = MarkTopicsCompletedFragment
        .newInstance(internalProfileId)
      activity.supportFragmentManager.beginTransaction().add(
        R.id.mark_topics_completed_container,
        markTopicsCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkTopicsCompletedFragment(): MarkTopicsCompletedFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.mark_topics_completed_container) as MarkTopicsCompletedFragment?
  }
}
