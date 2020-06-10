package org.oppia.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [CompletedStoryListActivity]. */
@ActivityScope
class CompletedStoryListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.completed_story_list_activity)
    if (getCompletedStoryListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.completed_story_list_fragment_placeholder,
        CompletedStoryListFragment.newInstance(internalProfileId)
      ).commitNow()
    }
  }

  private fun getCompletedStoryListFragment(): CompletedStoryListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.completed_story_list_fragment_placeholder
      ) as CompletedStoryListFragment?
  }
}
