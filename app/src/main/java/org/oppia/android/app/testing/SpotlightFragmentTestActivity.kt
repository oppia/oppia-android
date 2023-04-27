package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.testing.activity.TestActivity
import javax.inject.Inject

// TODO: Consolidate these up with the ones in TopicActivityPresenter & clean up.
private const val PROFILE_ID_ARGUMENT_KEY = "profile_id"

/** Test activity used for testing [SpotlightFragment]. */
class SpotlightFragmentTestActivity : TestActivity() {

  @Inject
  lateinit var spotlightFragmentTestActivityPresenter: SpotlightFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)

    spotlightFragmentTestActivityPresenter.handleOnCreate(
      intent.getIntExtra(
        PROFILE_ID_ARGUMENT_KEY, /* profileIdKeyDefaultValue= */ -1
      )
    )
  }

  /** Returns the spotlight fragment. */
  fun getSpotlightFragment() = spotlightFragmentTestActivityPresenter.getSpotlightFragment()

  /** Returns a view to be used as a spotlight anchor. */
  fun getSampleSpotlightTarget() = spotlightFragmentTestActivityPresenter.getSampleSpotlightTarget()

  interface Injector {
    fun inject(activity: SpotlightFragmentTestActivity)
  }

  companion object {
    /** Returns the [Intent] for opening [SpotlightFragmentTestActivity]. */
    fun createIntent(context: Context): Intent = createIntent(context, internalProfileId = 0)

    fun createIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, SpotlightFragmentTestActivity::class.java).also {
        it.putExtra(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      }
    }
  }
}
