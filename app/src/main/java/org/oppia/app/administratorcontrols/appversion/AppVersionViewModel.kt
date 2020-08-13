package org.oppia.app.administratorcontrols.appversion

import android.content.Context
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
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

  private val context: Context = fragment.activity!!.applicationContext

  val versionName: String = context.packageManager
    .getPackageInfo(context.packageName, 0).versionName

  private val lastUpdateDateTime =
    fragment.activity!!.packageManager.getPackageInfo(
      fragment.activity!!.packageName,
      /* flags= */ 0
    ).lastUpdateTime
  val lastUpdateDate = ObservableField<String>(getDateTime(lastUpdateDateTime))

  private fun getDateTime(lastUpdateTime: Long): String? {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_YYYY,
      lastUpdateTime,
      Locale.US
    )
  }
}
