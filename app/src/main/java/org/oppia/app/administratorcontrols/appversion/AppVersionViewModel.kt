package org.oppia.app.administratorcontrols.appversion

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import org.oppia.app.BuildConfig
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.util.system.OppiaDateTimeFormatter
import java.util.*
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

  // TODO(#555): Create one central utility file from where we should access date format or even convert date timestamp to string from that file.
  private fun getDateTime(l: Long): String? {
    val dateTime = oppiaDateTimeFormatter.formatDateFromDateString(
      oppiaDateTimeFormatter.dd_MMMM_yyyy,
      l,
      Locale.US
    )
    return dateTime
  }
}
