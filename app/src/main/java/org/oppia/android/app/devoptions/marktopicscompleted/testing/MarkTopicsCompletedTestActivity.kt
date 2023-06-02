package org.oppia.android.app.devoptions.marktopicscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedFragment

/** The activity for testing [MarkTopicsCompletedFragment]. */
class MarkTopicsCompletedTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_topics_completed_activity)
    internalProfileId = intent.getIntExtra(PROFILE_ID_EXTRA_KEY, -1)
    if (getMarkTopicsCompletedFragment() == null) {
      val markTopicsCompletedFragment = MarkTopicsCompletedFragment.newInstance(internalProfileId)
      supportFragmentManager.beginTransaction().add(
        R.id.mark_topics_completed_container,
        markTopicsCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkTopicsCompletedFragment(): MarkTopicsCompletedFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.mark_topics_completed_container) as MarkTopicsCompletedFragment?
  }

  companion object {
    const val PROFILE_ID_EXTRA_KEY = "MarkTopicsCompletedTestActivity.profile_id"

    /** Returns an [Intent] for [MarkTopicsCompletedTestActivity]. */
    fun createMarkTopicsCompletedTestIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, MarkTopicsCompletedTestActivity::class.java)
      intent.putExtra(PROFILE_ID_EXTRA_KEY, internalProfileId)
      return intent
    }
  }
}
