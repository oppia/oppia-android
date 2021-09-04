package org.oppia.android.domain.locale

import java.util.Locale

data class AndroidLocaleProfile(val languageCode: String, val regionCode: String) {
  fun matches(
    machineLocale: OppiaLocale.MachineLocale,
    otherProfile: AndroidLocaleProfile
  ): Boolean {
    return machineLocale.run {
      languageCode.equalsIgnoreCase(otherProfile.languageCode)
    } && machineLocale.run {
      regionCode.equalsIgnoreCase(otherProfile.regionCode)
    }
  }

  companion object {
    fun createFrom(androidLocale: Locale): AndroidLocaleProfile =
      AndroidLocaleProfile(androidLocale.language, androidLocale.country)
  }
}
