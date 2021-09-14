package org.oppia.android.domain.locale

import java.util.Locale
import org.oppia.android.util.locale.OppiaLocale

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

  fun computeIetfLanguageTag(): String {
    return if (regionCode.isNotEmpty()) {
      "$languageCode-$regionCode"
    } else languageCode
  }

  companion object {
    fun createFrom(androidLocale: Locale): AndroidLocaleProfile =
      AndroidLocaleProfile(androidLocale.language, androidLocale.country)
  }
}
