package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.MarkChaptersCompletedActivityArguments
import org.oppia.android.app.model.ScreenName.MARK_CHAPTERS_COMPLETED_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for Mark Chapters Completed. */
class MarkChaptersCompletedActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var markChaptersCompletedActivityPresenter: MarkChaptersCompletedActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val args = intent.getProtoExtra(
      MARKCHAPTERSCOMPLETED_ACTIVITY_ARGUMENTS,
      MarkChaptersCompletedActivityArguments.getDefaultInstance()
    )

    val internalProfileId = args?.profileId ?: -1
    val showConfirmationNotice = args?.showConfirmationNotice ?: false
    markChaptersCompletedActivityPresenter.handleOnCreate(internalProfileId, showConfirmationNotice)
    title = resourceHandler.getStringInLocale(R.string.mark_chapters_completed_activity_title)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    const val MARKCHAPTERSCOMPLETED_ACTIVITY_ARGUMENTS =
      "MarkChaptersCompletedActivity.Arguments"

    /** Returns an [Intent] to start this activity. */
    fun createMarkChaptersCompletedIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedActivity::class.java)

      val args = MarkChaptersCompletedActivityArguments.newBuilder().apply {
        this.profileId = internalProfileId
        this.showConfirmationNotice = showConfirmationNotice
      }
        .build()

      intent.putProtoExtra(MARKCHAPTERSCOMPLETED_ACTIVITY_ARGUMENTS, args)
      intent.decorateWithScreenName(MARK_CHAPTERS_COMPLETED_ACTIVITY)
      return intent
    }
  }
}
