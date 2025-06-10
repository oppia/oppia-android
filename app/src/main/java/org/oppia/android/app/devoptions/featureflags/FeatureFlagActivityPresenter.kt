package org.oppia.android.app.devoptions.featureflags

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import javax.inject.Inject


/** The presenter for [FeatureFlagActivity]. */
@ActivityScope
class FeatureFlagActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Called when [FeatureFlagActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.feature_flag_activity)

    if (getFeatureFlagFragment() == null) {
      val FeatureFlagFragment = FeatureFlagFragment.newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.feature_flag_container,
        FeatureFlagFragment
      ).commitNow()
    }
  }

  private fun getFeatureFlagFragment(): FeatureFlagFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.feature_flag_container) as FeatureFlagFragment?
  }
}
