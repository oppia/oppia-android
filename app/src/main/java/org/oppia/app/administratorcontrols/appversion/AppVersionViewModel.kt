package org.oppia.app.administratorcontrols.appversion

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import org.oppia.app.BuildConfig
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.util.system.OppiaDateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** [ViewModel] for [AppVersionFragment]*/
@FragmentScope
class AppVersionViewModel @Inject constructor(
  fragment: Fragment,
  private val oppiaDateTimeFormatter: OppiaDateTimeFormatter
) : ObservableViewModel() {

  val versionName = ObservableField<String>(BuildConfig.VERSION_NAME)

  private val lastUpdateDateTime =
    fragment.activity!!.packageManager.getPackageInfo(fragment.activity!!.packageName, /* flags= */ 0).lastUpdateTime
  val lastUpdateDate = ObservableField<String>(getDateTime(lastUpdateDateTime))

  private fun getDateTime(lastUpdateTime: Long): String? {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_YYYY,
      lastUpdateTime,
      Locale.US
    )
  }
}
