package org.oppia.android.app.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.databinding.SpotlightFragmentTestActivityBinding
import javax.inject.Inject

/** The presenter for [SpotlightFragmentTestActivity] */
@ActivityScope
class SpotlightFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  private lateinit var binding: SpotlightFragmentTestActivityBinding

  /** Handles onCreate() method of the [SpotlightFragmentTestActivity]. */
  fun handleOnCreate(internalProfileId: Int) {
    binding = SpotlightFragmentTestActivityBinding.inflate(activity.layoutInflater)
    activity.setContentView(binding.root)

    if (getSpotlightFragment() == null) {
      val spotlightFragment = SpotlightFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      spotlightFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.test_spotlight_overlay_placeholder,
        spotlightFragment, SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
  }

  /** Returns the spotlight fragment. */
  fun getSpotlightFragment(): SpotlightFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightFragment
  }

  /** Returns a view to be used as a spotlight anchor. */
  fun getSampleSpotlightTarget() = binding.sampleSpotlightTarget
}
