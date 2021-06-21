package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

const val MARK_CHAPTERS_COMPLETED_FRAGMENT = "MARK_CHAPTERS_COMPLETED_FRAGMENT"
const val MARK_STORIES_COMPLETED_FRAGMENT = "MARK_STORIES_COMPLETED_FRAGMENT"
const val MARK_TOPICS_COMPLETED_FRAGMENT = "MARK_TOPICS_COMPLETED_FRAGMENT"
const val EVENT_LOGS_FRAGMENT = "EVENT_LOGS_FRAGMENT"
const val FORCE_NETWORK_TYPE_FRAGMENT = "FORCE_NETWORK_TYPE_FRAGMENT"

/** Activity for Developer Options. */
class DeveloperOptionsActivity :
  InjectableAppCompatActivity(),
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener {
  @Inject
  lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    developerOptionsActivityPresenter.handleOnCreate()
    title = getString(R.string.developer_options_activity_title)
  }

  override fun routeToMarkChaptersCompleted() {
    startActivity(
      MarkChaptersCompletedActivity
        .createMarkChaptersCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToMarkStoriesCompleted() {
    startActivity(
      MarkStoriesCompletedActivity
        .createMarkStoriesCompletedIntent(this, internalProfileId)
    )
  }

  companion object {
    /** Function to create intent for DeveloperOptionsActivity */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, internalProfileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }
}
