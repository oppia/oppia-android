package org.oppia.android.app.devoptions.featureflags.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.featureflags.FeatureFlagsFragment
import org.oppia.android.app.ui.R

/** Activity for testing [FeatureFlagsFragment]. */
class FeatureFlagsTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.feature_flags_activity)
    if (getFeatureFlagsFragment() == null) {
      val featureFlagsFragment = FeatureFlagsFragment.newInstance()
      supportFragmentManager.beginTransaction().add(
        R.id.feature_flags_container,
        featureFlagsFragment
      ).commitNow()
    }
  }

  private fun getFeatureFlagsFragment(): FeatureFlagsFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.feature_flags_container) as? FeatureFlagsFragment
  }
}
