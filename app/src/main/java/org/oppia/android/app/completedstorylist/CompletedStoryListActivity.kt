package org.oppia.android.app.completedstorylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.COMPLETED_STORY_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for completed stories. */
class CompletedStoryListActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var completedStoryListActivityPresenter: CompletedStoryListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()
    completedStoryListActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.

    /** Returns a new [Intent] to route to [CompletedStoryListActivity] for a specified profile ID. */
    fun createCompletedStoryListActivityIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
      val intent = Intent(context, CompletedStoryListActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(COMPLETED_STORY_LIST_ACTIVITY)
      }

      return intent
    }
  }
}
