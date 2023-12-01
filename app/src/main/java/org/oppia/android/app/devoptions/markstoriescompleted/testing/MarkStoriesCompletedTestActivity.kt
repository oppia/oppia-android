package org.oppia.android.app.devoptions.markstoriescompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedFragment
import org.oppia.android.app.model.MarkStoriesCompletedTestActivityArguments
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

/** Activity for testing [MarkStoriesCompletedFragment]. */
class MarkStoriesCompletedTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_stories_completed_activity)
    val args = intent.getProtoExtra(
      MARKSTORIESCOMPLETEDTESTACTIVITY_ARGUMENTS_KEY,
      MarkStoriesCompletedTestActivityArguments.getDefaultInstance()
    )

    internalProfileId = args?.profileId ?: -1
    if (getMarkStoriesCompletedFragment() == null) {
      val markStoriesCompletedFragment = MarkStoriesCompletedFragment.newInstance(internalProfileId)
      supportFragmentManager.beginTransaction().add(
        R.id.mark_stories_completed_container,
        markStoriesCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkStoriesCompletedFragment(): MarkStoriesCompletedFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.mark_stories_completed_container) as MarkStoriesCompletedFragment?
  }

  companion object {
    /** [String] key value for mapping to InternalProfileId in [Bundle]. */
    const val PROFILE_ID_EXTRA_KEY = "MarkStoriesCompletedTestActivity.profile_id"

    /** Argument key for MarkStoriesCompletedTestActivity. */
    const val MARKSTORIESCOMPLETEDTESTACTIVITY_ARGUMENTS_KEY =
      "MarkStoriesCompletedTestActivity.Arguments"

    /** Returns an [Intent] for [MarkStoriesCompletedTestActivity]. */
    fun createMarkStoriesCompletedTestIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkStoriesCompletedTestActivity::class.java)
      val args = MarkStoriesCompletedTestActivityArguments.newBuilder()
        .apply {
          profileId = internalProfileId
        }.build()
      intent.putProtoExtra(MARKSTORIESCOMPLETEDTESTACTIVITY_ARGUMENTS_KEY, args)
      return intent
    }
  }
}
