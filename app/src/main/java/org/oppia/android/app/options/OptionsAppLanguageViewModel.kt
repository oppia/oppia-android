package org.oppia.android.app.options

import org.oppia.android.app.model.OppiaLanguage

/** App language settings view model for the recycler view in [OptionsFragment]. */
class OptionsAppLanguageViewModel(
  private val routeToAppLanguageListListener: RouteToAppLanguageListListener,
  private var loadAppLanguageListListener: LoadAppLanguageListListener,
  val oppiaLanguage: OppiaLanguage,
  val appLanguageDisplayName: String
) : OptionsItemViewModel() {

  fun onAppLanguageClicked() {
    if (isMultipane.get()!!) {
      loadAppLanguageListListener.loadAppLanguageFragment(oppiaLanguage)
    } else {
      routeToAppLanguageListListener.routeAppLanguageList(oppiaLanguage)
    }
  }
}
