package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.utility.getLastUpdateTime
import org.oppia.android.app.utility.getVersionName
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.system.OppiaDateTimeFormatter
import java.util.*
import javax.inject.Inject

/** [ViewModel] for [AppVersionFragment]*/
@FragmentScope
class AppVersionViewModel @Inject constructor(
  fragment: Fragment,
  private val oppiaDateTimeFormatter: OppiaDateTimeFormatter,
  context: Context
) : ObservableViewModel() {

  val versionName: String = context.getVersionName()

  private val lastUpdateDateTime = context.getLastUpdateTime()
  val lastUpdateDate = ObservableField<String>(getDateTime(lastUpdateDateTime))

  private fun getDateTime(lastUpdateTime: Long): String? {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_YYYY,
      lastUpdateTime,
      Locale.US
    )
  }
}
