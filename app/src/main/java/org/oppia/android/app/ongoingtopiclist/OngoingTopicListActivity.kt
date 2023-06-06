package org.oppia.android.app.ongoingtopiclist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.ONGOING_TOPIC_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for ongoing topics. */
class OngoingTopicListActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var ongoingTopicListActivityPresenter: OngoingTopicListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId: Int = intent.getIntExtra(
      ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY, -1
    )
    ongoingTopicListActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY =
      "OngoingTopicListActivity.profile_id"

    /** Returns a new [Intent] to route to [OngoingTopicListActivity] for a specified profile ID. */
    fun createOngoingTopicListActivityIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, OngoingTopicListActivity::class.java).apply {
        putExtra(ONGOING_TOPIC_LIST_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
        decorateWithScreenName(ONGOING_TOPIC_LIST_ACTIVITY)
      }
    }
  }
}
