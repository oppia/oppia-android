package org.oppia.android.app.devoptions.markstoriescompleted

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.utility.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [MarkStoriesCompletedActivity]. */
@ActivityScope
class MarkStoriesCompletedActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate(internalProfileId: Int) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.mark_stories_completed_activity)

    if (getMarkStoriesCompletedFragment() == null) {
      val markStoriesCompletedFragment = MarkStoriesCompletedFragment
        .newInstance(internalProfileId)
      activity.supportFragmentManager.beginTransaction().add(
        R.id.mark_stories_completed_container,
        markStoriesCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkStoriesCompletedFragment(): MarkStoriesCompletedFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.mark_stories_completed_container) as MarkStoriesCompletedFragment?
  }
}
