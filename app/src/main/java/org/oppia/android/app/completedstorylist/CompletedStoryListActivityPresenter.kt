package org.oppia.android.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [CompletedStoryListActivity]. */
@ActivityScope
class CompletedStoryListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Initializes views for [CompletedStoryListActivity] and binds [CompletedStoryListFragment]. */
  fun handleOnCreate(profileId: LegacyProfileId) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.completed_story_list_activity)
    if (getCompletedStoryListFragment() == null) {
      activity
        .supportFragmentManager
        .beginTransaction()
        .add(
          R.id.completed_story_list_fragment_placeholder,
          CompletedStoryListFragment.newInstance(profileId),
          CompletedStoryListFragment.COMPLETED_STORY_LIST_FRAGMENT_TAG
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
