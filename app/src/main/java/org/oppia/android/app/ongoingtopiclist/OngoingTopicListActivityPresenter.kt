package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [OngoingTopicListActivity]. */
@ActivityScope
class OngoingTopicListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  fun handleOnCreate(internalProfileId: Int) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.ongoing_topic_list_activity)
    if (getOngoingTopicListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.ongoing_topic_list_fragment_placeholder,
        OngoingTopicListFragment.newInstance(internalProfileId),
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
