package org.oppia.android.app.administratorcontrols.appversion

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [AppVersionActivity]. */
@ActivityScope
class AppVersionActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Initializes the [AppVersionActivity] views and binds [AppVersionFragment]. */
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.app_version_activity)
    val toolbar = setToolbar()
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        toolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
    if (getAppVersionFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.app_version_fragment_placeholder,
        AppVersionFragment()
      ).commitNow()
    }
  }

  private fun setToolbar(): Toolbar {
    val appVersionToolbar: Toolbar = activity.findViewById(R.id.app_version_toolbar) as Toolbar
    activity.setSupportActionBar(appVersionToolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    return appVersionToolbar
  }

  private fun getAppVersionFragment(): AppVersionFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.app_version_fragment_placeholder) as AppVersionFragment?
  }
}
