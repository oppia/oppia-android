package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Test activity used for testing [SpotlightFragment]. */
class SpotlightFragmentTestActivity : TestActivity() {

  @Inject
  lateinit var spotlightFragmentTestActivityPresenter: SpotlightFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    spotlightFragmentTestActivityPresenter.handleOnCreate(
      intent.getIntExtra(
        PROFILE_ID_ARGUMENT_KEY, /* profileIdKeyDefaultValue */ -1
      )
    )
  }

  /** Returns the spotlight fragment. */
  fun getSpotlightFragment() = spotlightFragmentTestActivityPresenter.getSpotlightFragment()

  /** Returns a view to be used as a spotlight anchor. */
  fun getSampleSpotlightTarget() = spotlightFragmentTestActivityPresenter.getSampleSpotlightTarget()

  companion object {
    /** Returns the [Intent] for opening [SpotlightFragmentTestActivity]. */
    fun createSpotlightFragmentTestActivity(context: Context): Intent {
      return Intent(context, SpotlightFragmentTestActivity::class.java).also {
        it.putExtra(PROFILE_ID_ARGUMENT_KEY, /* profileIdKeyDefaultValue */ 0)
      }
    }
  }
}
