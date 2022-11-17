package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import javax.inject.Inject
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY

class SpotlightFragmentTestActivity : TestActivity() {

  @Inject
  lateinit var spotlightFragmentTestActivityPresenter: SpotlightFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    spotlightFragmentTestActivityPresenter.handleOnCreate(
      intent.getIntExtra(
        PROFILE_ID_ARGUMENT_KEY, -1
      )
    )
  }

  fun getSpotlightFragment() = spotlightFragmentTestActivityPresenter.getSpotlightFragment()

  fun getSampleSpotlightTarget() = spotlightFragmentTestActivityPresenter.getSampleSpotlightTarget()

  companion object {
    /** Returns the [Intent] for opening [SpotlightFragmentTestActivity]. */
    fun createSpotlightFragmentTestActivity(context: Context): Intent {
      return Intent(context, SpotlightFragmentTestActivity::class.java).also {
        it.putExtra(PROFILE_ID_ARGUMENT_KEY, 0)
      }
    }
  }
}