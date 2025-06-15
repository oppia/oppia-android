package org.oppia.android.app.devoptions.featureflags.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.featureflags.FeatureFlagFragment
import org.oppia.android.app.ui.R

/** Activity for testing [FeatureFlagFragment]. */
class FeatureFlagTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.feature_flag_activity)
    if (getFeatureFlagFragment() == null) {
      val FeatureFlagFragment = FeatureFlagFragment.newInstance()
      supportFragmentManager.beginTransaction().add(
        R.id.feature_flag_container,
        FeatureFlagFragment
      ).commitNow()
    }
  }

  private fun getFeatureFlagFragment(): FeatureFlagFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.feature_flag_container) as FeatureFlagFragment?
  }
}
