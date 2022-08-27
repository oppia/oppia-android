package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.MARK_TOPICS_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Mark Topics Completed. */
class MarkTopicsCompletedActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var markTopicsCompletedActivityPresenter: MarkTopicsCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    markTopicsCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.mark_topics_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val PROFILE_ID_EXTRA_KEY = "MarkTopicsCompletedActivity.profile_id"

    fun createMarkTopicsCompletedIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, MarkTopicsCompletedActivity::class.java).apply {
        putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
        decorateWithScreenName(MARK_TOPICS_COMPLETED_ACTIVITY)
      }
    }
  }
}
