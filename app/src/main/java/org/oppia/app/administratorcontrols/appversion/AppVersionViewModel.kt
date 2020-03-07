package org.oppia.app.administratorcontrols.appversion

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import org.oppia.app.BuildConfig
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/** [ViewModel] for [AppVersionFragment]*/
@FragmentScope
class AppVersionViewModel @Inject constructor(
  private val fragment: Fragment
) : ObservableViewModel() {

  val versionName = ObservableField<String>(BuildConfig.VERSION_NAME)

  private val lastUpdateDateTime =
    fragment.activity!!.packageManager.getPackageInfo(fragment.activity!!.packageName, 0).lastUpdateTime
  val lastUpdateDate = ObservableField<String>(getDateTime(lastUpdateDateTime))

  private fun getDateTime(l: Long): String? {
    return try {
      val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
      val netDate = Date(l)
      sdf.format(netDate)
    } catch (e: Exception) {
      e.toString()
    }
  }
}
