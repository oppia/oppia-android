package org.oppia.android.domain.locale

import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.UNRECOGNIZED
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED
import org.oppia.android.app.model.OppiaRegion

sealed class OppiaLocale(val localeContext: OppiaLocaleContext) {
  // TODO: verify exclusivity of regions/languages table in tests.

  fun getCurrentLanguage(): OppiaLanguage = localeContext.languageDefinition.language

  fun getLanguageId(): LanguageId = when (localeContext.usageMode) {
    APP_STRINGS -> localeContext.languageDefinition.appStringId
    CONTENT_STRINGS -> localeContext.languageDefinition.contentStringId
    AUDIO_TRANSLATIONS -> localeContext.languageDefinition.audioTranslationId
    USAGE_MODE_UNSPECIFIED, UNRECOGNIZED, null -> LanguageId.getDefaultInstance()
  }

  fun getFallbackLanguageId(): LanguageId = when (localeContext.usageMode) {
    APP_STRINGS -> localeContext.fallbackLanguageDefinition.appStringId
    CONTENT_STRINGS -> localeContext.fallbackLanguageDefinition.contentStringId
    AUDIO_TRANSLATIONS -> localeContext.fallbackLanguageDefinition.audioTranslationId
    USAGE_MODE_UNSPECIFIED, UNRECOGNIZED, null -> LanguageId.getDefaultInstance()
  }

  fun getCurrentRegion(): OppiaRegion = localeContext.regionDefinition.region

  // TODO: documentation (https://developer.android.com/reference/java/util/Locale).
  abstract class MachineLocale(localeContext: OppiaLocaleContext): OppiaLocale(localeContext) {
    abstract fun String.formatForMachines(vararg args: Any?): String

    abstract fun String.toMachineLowerCase(): String

    abstract fun String.toMachineUpperCase(): String

    abstract fun String.capitalizeForMachines(): String

    abstract fun String.decapitalizeForMachines(): String

    // TODO: regex to block ignoreCase.
    abstract fun String?.equalsIgnoreCase(other: String?): Boolean

    // TODO: documentation. See below.
    abstract fun getCurrentTimeOfDay(): TimeOfDay?

    // TODO: documentation. Explain this is always corresponding to the local timezone of the device
    //  (which isn't tied to the locale).
    abstract fun parseOppiaDate(dateString: String): OppiaDate?

    enum class TimeOfDay {
      MORNING,
      AFTERNOON,
      EVENING
    }

    interface OppiaDate {
      fun isBeforeToday(): Boolean
    }
  }

  abstract class DisplayLocale(localeContext: OppiaLocaleContext): OppiaLocale(localeContext) {
    abstract fun getCurrentDateString(): String

    abstract fun getCurrentTimeString(): String

    abstract fun getCurrentDateTimeString(): String

    // TODO: mention bidi wrapping & machine readable args
    // TODO: document that receiver is the format (unlike String.format()).
    abstract fun String.formatInLocale(vararg args: Any?): String

    abstract fun Resources.getStringInLocale(@StringRes id: Int): String

    abstract fun Resources.getStringInLocale(@StringRes id: Int, vararg formatArgs: Any?): String

    // TODO: for this & others, document that they won't necessarily follow the locale of this object
    //  (they actually depend on the locale specified in Resources).
    abstract fun Resources.getStringArrayInLocale(@ArrayRes id: Int): List<String>
  }

  class ContentLocale(localeContext: OppiaLocaleContext): OppiaLocale(localeContext)
}
