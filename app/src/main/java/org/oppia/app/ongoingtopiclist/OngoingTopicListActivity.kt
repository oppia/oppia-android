package org.oppia.app.ongoingtopiclist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for ongoing topics. */
class OngoingTopicListActivity : InjectableAppCompatActivity() {
  @Inject lateinit var ongoingTopicListActivityPresenter: OngoingTopicListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId: Int = intent.getIntExtra(ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY, -1)
    ongoingTopicListActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    internal const val ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY = "OngoingTopicListActivity.profile_id"

    /** Returns a new [Intent] to route to [OngoingTopicListActivity] for a specified profile ID. */
    fun createOngoingTopicListActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, OngoingTopicListActivity::class.java)
      intent.putExtra(ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
