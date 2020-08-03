package org.oppia.app.options

import android.util.Log
import androidx.databinding.ObservableField

/** App language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAppLanguageViewModel(
  private val routeToAppLanguageListListener: RouteToAppLanguageListListener
) : OptionsItemViewModel() {
  val appLanguage = ObservableField<String>("")

  fun setAppLanguage(appLanguageValue: String) {
    appLanguage.set(appLanguageValue)
  }

  fun onAppLanguageClicked() {
    if (isMultipaneOptions.get()!!) {
      Log.d("Multipane", "OptionsAppLanguageViewModel")
      // TODO add this fragment to "multipaneOptionsContainer"
    } else {
      routeToAppLanguageListListener.routeAppLanguageList(appLanguage.get())
    }
  }
}
