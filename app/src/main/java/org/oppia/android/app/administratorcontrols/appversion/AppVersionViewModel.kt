package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.getLastUpdateTime
import org.oppia.android.app.utility.getVersionName
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [AppVersionFragment]*/
@FragmentScope
class AppVersionViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
  context: Context
) : ObservableViewModel() {

  private val versionName: String = context.getVersionName()
  private val lastUpdateDateTime = context.getLastUpdateTime()

  fun computeVersionNameText(): String =
    resourceHandler.getStringInLocaleWithWrapping(R.string.administrator_controls_activity_label_title_app_version_activity_app_version_name, versionName)

  fun computeLastUpdatedDateText(): String =
    resourceHandler.getStringInLocaleWithWrapping(
      R.string.administrator_controls_activity_label_title_app_version_activity_app_last_update_date, getDateTime(lastUpdateDateTime)
    )

  private fun getDateTime(lastUpdateTime: Long): String =
    resourceHandler.computeDateString(lastUpdateTime)
}
