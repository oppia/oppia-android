package org.oppia.app.options

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
    routeToAppLanguageListListener.routeAppLanguageList(appLanguage.get())
  }
}
