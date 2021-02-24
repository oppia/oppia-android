package org.oppia.android.app.options

import androidx.databinding.ObservableField

/** App language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAppLanguageViewModel(
  private val routeToAppLanguageListListener: RouteToAppLanguageListListener,
  private var loadAppLanguageListListener: LoadAppLanguageListListener
) : OptionsItemViewModel() {
  val appLanguage = ObservableField<String>("")

  fun setAppLanguage(appLanguageValue: String) {
    appLanguage.set(appLanguageValue)
  }

  fun onAppLanguageClicked() {
    if (isMultipane.get()!!) {
      loadAppLanguageListListener.loadAppLanguageFragment(appLanguage.get()!!)
    } else {
      routeToAppLanguageListListener.routeAppLanguageList(appLanguage.get())
    }
  }
}
