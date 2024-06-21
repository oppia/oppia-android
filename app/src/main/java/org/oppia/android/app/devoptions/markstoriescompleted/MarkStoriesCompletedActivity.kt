package org.oppia.android.app.devoptions.markstoriescompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.MARK_STORIES_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Mark Stories Completed. */
class MarkStoriesCompletedActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var markStoriesCompletedActivityPresenter: MarkStoriesCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    markStoriesCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.mark_stories_completed_activity_title)
    handleBackPress()
  }

  private fun handleBackPress() {
    onBackPressedDispatcher.addCallback(
      this@MarkStoriesCompletedActivity,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
         isEnabled = false
         onBackPressedDispatcher.onBackPressed()
         isEnabled = true
        }
      }
    )
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressedDispatcher.onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    /** [String] key value for mapping to InternalProfileId in [Bundle]. */
    const val PROFILE_ID_EXTRA_KEY = "MarkStoriesCompletedActivity.profile_id"

    /** Returns an [Intent] to start this activity. */
    fun createMarkStoriesCompletedIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, MarkStoriesCompletedActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
        decorateWithScreenName(MARK_STORIES_COMPLETED_ACTIVITY)
      }
    }
  }
}
