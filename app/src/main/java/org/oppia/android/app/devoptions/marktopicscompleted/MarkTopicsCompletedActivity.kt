package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.markstoriescompleted.testing.MarkStoriesCompletedTestActivity
import org.oppia.android.app.model.MarkTopicsCompletedActivityArguments
import org.oppia.android.app.model.ScreenName.MARK_TOPICS_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Mark Topics Completed. */
class MarkTopicsCompletedActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var markTopicsCompletedActivityPresenter: MarkTopicsCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      MARK_TOPICS_COMPLETED_ACTIVITY_ARGUMENTS_KEY,
      MarkTopicsCompletedActivityArguments.getDefaultInstance()
    )
    internalProfileId = args?.profileId ?: -1
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
    /** Argument key for MarkTopicsCompletedActivity.. */
    const val MARK_TOPICS_COMPLETED_ACTIVITY_ARGUMENTS_KEY =
      "MarkTopicsCompletedActivity.arguments"

    /** Returns an [Intent] for [MarkStoriesCompletedTestActivity]. */
    fun createMarkTopicsCompletedIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, MarkTopicsCompletedActivity::class.java).apply {
        val args = MarkTopicsCompletedActivityArguments.newBuilder().apply {
          profileId = internalProfileId
        }.build()
        putProtoExtra(MARK_TOPICS_COMPLETED_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(MARK_TOPICS_COMPLETED_ACTIVITY)
      }
    }
  }
}
