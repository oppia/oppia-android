package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.MarkChaptersCompletedActivityParams
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
      MARK_CHAPTERS_COMPLETED_ACTIVITY_PARAMS,
      MarkChaptersCompletedActivityParams.getDefaultInstance()
    )

    val internalProfileId = args?.internalProfileId ?: -1
    val showConfirmationNotice = args?.showConfirmationNotice ?: false
    markChaptersCompletedActivityPresenter.handleOnCreate(internalProfileId, showConfirmationNotice)
    title = resourceHandler.getStringInLocale(R.string.mark_chapters_completed_activity_title)

    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(/* enabled = */ true) {
      override fun handleOnBackPressed() {
        finish()
      }
    })
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressedDispatcher.onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    /** Params key for [MarkChaptersCompletedActivity]. */
    const val MARK_CHAPTERS_COMPLETED_ACTIVITY_PARAMS =
      "MarkChaptersCompletedActivity.params"

    /** Returns an [Intent] to start this activity. */
    fun createMarkChaptersCompletedIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedActivity::class.java)

      val args = MarkChaptersCompletedActivityParams.newBuilder().apply {
        this.internalProfileId = internalProfileId
        this.showConfirmationNotice = showConfirmationNotice
      }
        .build()

      intent.putProtoExtra(MARK_CHAPTERS_COMPLETED_ACTIVITY_PARAMS, args)
      intent.decorateWithScreenName(MARK_CHAPTERS_COMPLETED_ACTIVITY)
      return intent
    }
  }
}
