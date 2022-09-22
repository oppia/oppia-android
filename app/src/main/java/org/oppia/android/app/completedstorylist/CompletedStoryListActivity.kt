package org.oppia.android.app.completedstorylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.COMPLETED_STORY_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for completed stories. */
class CompletedStoryListActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var completedStoryListActivityPresenter: CompletedStoryListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId: Int =
      intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    completedStoryListActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val PROFILE_ID_EXTRA_KEY =
      "CompletedStoryListActivity.profile_id"

    /** Returns a new [Intent] to route to [CompletedStoryListActivity] for a specified profile ID. */
    fun createCompletedStoryListActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, CompletedStoryListActivity::class.java)
      intent.putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
      intent.decorateWithScreenName(COMPLETED_STORY_LIST_ACTIVITY)
      return intent
    }
  }
}
