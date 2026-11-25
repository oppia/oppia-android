package org.oppia.android.app.devoptions.viewlogs

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Sets up [ViewPerformanceMetricsActivity] using the shared app toolbar and fragment pattern. */
@ActivityScope
class ViewPerformanceMetricsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.view_performance_metrics_activity)
    val existingFragment = activity.supportFragmentManager
      .findFragmentById(R.id.performance_metrics_container)
    if (existingFragment == null) {
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.performance_metrics_container, ViewPerformanceMetricsFragment.newInstance())
        .commitNow()
    }
  }
}
