package org.oppia.android.app.devoptions.markchapterscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment
import org.oppia.android.app.model.MarkChaptersCompletedTestActivityParams
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

/** The activity for testing [MarkChaptersCompletedFragment]. */
class MarkChaptersCompletedTestActivity : InjectableSystemLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_chapters_completed_activity)

    val args = intent.getProtoExtra(
      MARK_CHAPTERS_COMPLETED_TEST_ACTIVITY_PARAMS_KEY,
      MarkChaptersCompletedTestActivityParams.getDefaultInstance()
    )

    val internalProfileId = args?.internalProfileId ?: -1
    val showConfirmationNotice = args?.showConfirmationNotice ?: false
    if (getMarkChaptersCompletedFragment() == null) {
      val markChaptersCompletedFragment =
        MarkChaptersCompletedFragment.newInstance(internalProfileId, showConfirmationNotice)
      supportFragmentManager.beginTransaction().add(
        R.id.mark_chapters_completed_container,
        markChaptersCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkChaptersCompletedFragment(): MarkChaptersCompletedFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.mark_chapters_completed_container) as MarkChaptersCompletedFragment?
  }

  companion object {
    /** Params key for MarkChaptersCompletedTestActivity. */
    private const val MARK_CHAPTERS_COMPLETED_TEST_ACTIVITY_PARAMS_KEY =
      "MarkChaptersCompletedTestActivity.params"

    /** Returns an [Intent] for [MarkChaptersCompletedTestActivity]. */
    fun createMarkChaptersCompletedTestIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedTestActivity::class.java)

      val args = MarkChaptersCompletedTestActivityParams.newBuilder().apply {
        this.internalProfileId = internalProfileId
        this.showConfirmationNotice = showConfirmationNotice
      }
        .build()

      intent.putProtoExtra(MARK_CHAPTERS_COMPLETED_TEST_ACTIVITY_PARAMS_KEY, args)
      return intent
    }
  }
}
