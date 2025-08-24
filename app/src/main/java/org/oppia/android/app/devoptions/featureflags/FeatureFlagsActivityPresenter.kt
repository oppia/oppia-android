package org.oppia.android.app.devoptions.featureflags

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** The presenter for [FeatureFlagsActivity]. */
@ActivityScope
class FeatureFlagsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Called when [FeatureFlagsActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.feature_flags_activity)

    if (getFeatureFlagsFragment() == null) {
      val featureFlagsFragment = FeatureFlagsFragment.newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.feature_flags_container,
        featureFlagsFragment
      ).commitNow()
    }
  }

  private fun getFeatureFlagsFragment(): FeatureFlagsFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.feature_flags_container) as? FeatureFlagsFragment
  }
}
