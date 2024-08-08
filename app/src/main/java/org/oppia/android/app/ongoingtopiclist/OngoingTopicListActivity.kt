package org.oppia.android.app.ongoingtopiclist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.ONGOING_TOPIC_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for ongoing topics. */
class OngoingTopicListActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var ongoingTopicListActivityPresenter: OngoingTopicListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId: Int = intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: -1
    ongoingTopicListActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    /** Returns a new [Intent] to route to [OngoingTopicListActivity] for a specified profile ID. */
    fun createOngoingTopicListActivityIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      return Intent(context, OngoingTopicListActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(ONGOING_TOPIC_LIST_ACTIVITY)
      }
    }
  }
}
