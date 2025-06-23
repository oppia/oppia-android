package org.oppia.android.app.devoptions.platformparameters.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.platformparameters.PlatformParameterFragment
import org.oppia.android.app.ui.R

/** Activity for testing [PlatformParameterFragment]. */
class PlatformParameterTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.platform_parameter_activity)
    if (getPlatformParameterFragment() == null) {
      val PlatformParameterFragment = PlatformParameterFragment.newInstance()
      supportFragmentManager.beginTransaction().add(
        R.id.platform_parameter_container,
        PlatformParameterFragment
      ).commitNow()
    }
  }

  private fun getPlatformParameterFragment(): PlatformParameterFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.platform_parameter_container) as PlatformParameterFragment?
  }
}
