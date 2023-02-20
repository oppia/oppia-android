package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import javax.inject.Inject

/** The presenter for [OngoingTopicListActivity]. */
@ActivityScope
class OngoingTopicListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.ongoing_topic_list_activity)
    if (getOngoingTopicListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.ongoing_topic_list_fragment_placeholder,
        OngoingTopicListFragment.newInstance(profileId),
        OngoingTopicListFragment.ONGOING_TOPIC_LIST_FRAGMENT_TAG
      ).commitNow()
    }
  }

  private fun getOngoingTopicListFragment(): OngoingTopicListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.ongoing_topic_list_fragment_placeholder
      ) as OngoingTopicListFragment?
  }
}
