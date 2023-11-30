package org.oppia.android.app.devoptions.markchapterscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedFragment
import org.oppia.android.app.model.MarkChaptersCompletedTestActivityArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** The activity for testing [MarkChaptersCompletedFragment]. */
class MarkChaptersCompletedTestActivity : InjectableSystemLocalizedAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_chapters_completed_activity)
    val args = intent.getBundleExtra(MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT_KEY)?.getProto(
      MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT_KEY,
      MarkChaptersCompletedTestActivityArguments.getDefaultInstance()
    )

    val internalProfileId = args?.profileId ?: -1
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
    private const val PROFILE_ID_EXTRA_KEY = "MarkChaptersCompletedTestActivity.profile_id"
    private const val SHOW_CONFIRMATION_NOTICE_EXTRA_KEY =
      "MarkChaptersCompletedTestActivity.show_confirmation_notice"
    private const val MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT_KEY =
      "MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT"

    /** Returns an [Intent] for [MarkChaptersCompletedTestActivity]. */
    fun createMarkChaptersCompletedTestIntent(
      context: Context,
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): Intent {
      val intent = Intent(context, MarkChaptersCompletedTestActivity::class.java)
      val bundle = Bundle().apply {
        val args = MarkChaptersCompletedTestActivityArguments.newBuilder().apply {
          this.profileId = internalProfileId
          this.showConfirmationNotice = showConfirmationNotice
        }
          .build()
        putProto(MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT_KEY, args)
      }
      intent.putExtra(MARKCHAPTERSCOMPLETEDTESTACTIVITY_ARGUMENT_KEY, bundle)
      return intent
    }
  }
}
