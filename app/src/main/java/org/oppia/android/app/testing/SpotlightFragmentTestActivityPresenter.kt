package org.oppia.android.app.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.topic.SPOTLIGHT_FRAGMENT_TAG
import org.oppia.android.databinding.SpotlightFragmentTestActivityBinding

@ActivityScope
class SpotlightFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
  ) {

  private lateinit var binding: SpotlightFragmentTestActivityBinding

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
        spotlightFragment, SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
  }

  fun getSpotlightFragment(): SpotlightFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      SPOTLIGHT_FRAGMENT_TAG
    ) as SpotlightFragment?
  }

  fun getSampleSpotlightTarget() = binding.sampleSpotlightTarget
}