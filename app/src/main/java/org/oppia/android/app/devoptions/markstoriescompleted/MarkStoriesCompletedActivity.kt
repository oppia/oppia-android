package org.oppia.android.app.devoptions.markstoriescompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.MarkStoriesCompletedActivityArguments
import org.oppia.android.app.model.ScreenName.MARK_STORIES_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
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

    val args = intent.getProtoExtra(
      MARK_STORIES_COMPLETED_ACTIVITY_ARGUMENTS_KEY,
      MarkStoriesCompletedActivityArguments.getDefaultInstance()
    )
    internalProfileId = args?.internalProfileId ?: -1
    markStoriesCompletedActivityPresenter.handleOnCreate(internalProfileId)
    title = resourceHandler.getStringInLocale(R.string.mark_stories_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    /** Argument key for MarkStoriesCompletedActivity.. */
    const val MARK_STORIES_COMPLETED_ACTIVITY_ARGUMENTS_KEY =
      "MarkStoriesCompletedActivity.arguments"

    /** Returns an [Intent] to start this activity. */
    fun createMarkStoriesCompletedIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, MarkStoriesCompletedActivity::class.java).apply {
        val args =
          MarkStoriesCompletedActivityArguments.newBuilder().setInternalProfileId(internalProfileId)
            .build()
        putProtoExtra(MARK_STORIES_COMPLETED_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(MARK_STORIES_COMPLETED_ACTIVITY)
      }
    }
  }
}
