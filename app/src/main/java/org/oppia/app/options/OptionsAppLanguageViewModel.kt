package org.oppia.app.options

import androidx.databinding.ObservableField

/** App language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAppLanguageViewModel(
  private val routeToAppLanguageListListener: RouteToAppLanguageListListener,
  private val loadAppLanguageFragmentListener: LoadAppLanguageFragmentListener
) : OptionsItemViewModel() {
  val appLanguage = ObservableField<String>("")

  fun setAppLanguage(appLanguageValue: String) {
    appLanguage.set(appLanguageValue)
  }

  fun loadAppLanguageFragment() {
    loadAppLanguageFragmentListener.loadAppLanguageFragment(appLanguage.get()!!)
  }

  fun onAppLanguageClicked() {
    if (isMultipaneOptions.get()!!) {
      loadAppLanguageFragment()
    } else {
      routeToAppLanguageListListener.routeAppLanguageList(appLanguage.get())
    }
  }
}
