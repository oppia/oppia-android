package org.oppia.android.app.completedstorylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for completed stories. */
class CompletedStoryListActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var completedStoryListActivityPresenter: CompletedStoryListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId: Int =
      intent.getIntExtra(COMPLETED_STORY_LIST_ACTIVITY_PROFILE_ID_KEY, -1)
    completedStoryListActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val COMPLETED_STORY_LIST_ACTIVITY_PROFILE_ID_KEY =
      "CompletedStoryListActivity.profile_id"

    /** Returns a new [Intent] to route to [CompletedStoryListActivity] for a specified profile ID. */
    fun createCompletedStoryListActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, CompletedStoryListActivity::class.java)
      intent.putExtra(COMPLETED_STORY_LIST_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
