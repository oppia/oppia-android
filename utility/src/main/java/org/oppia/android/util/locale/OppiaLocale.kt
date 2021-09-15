package org.oppia.android.util.locale

import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
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
import org.oppia.android.domain.locale.getFallbackLanguageId
import org.oppia.android.domain.locale.getLanguageId

// TOOD: document that equals, tostring, and hashcode are all properly implemented for subclasses?
sealed class OppiaLocale {
  abstract val localeContext: OppiaLocaleContext

  // TODO: verify exclusivity of regions/languages table in tests.

  fun getCurrentLanguage(): OppiaLanguage = localeContext.languageDefinition.language

  fun getLanguageId(): LanguageId = localeContext.getLanguageId()

  fun getFallbackLanguageId(): LanguageId = localeContext.getFallbackLanguageId()

  fun getCurrentRegion(): OppiaRegion = localeContext.regionDefinition.region

  // TODO: documentation (https://developer.android.com/reference/java/util/Locale). Document this is available everywhere in the app.
  abstract class MachineLocale(override val localeContext: OppiaLocaleContext): OppiaLocale() {
    abstract fun String.formatForMachines(vararg args: Any?): String

    abstract fun String.toMachineLowerCase(): String

    abstract fun String.toMachineUpperCase(): String

    abstract fun String.capitalizeForMachines(): String

    abstract fun String.decapitalizeForMachines(): String

    abstract fun String.endsWithIgnoreCase(suffix: String): Boolean

    abstract fun String?.equalsIgnoreCase(other: String?): Boolean

    // TODO: documentation. See below.
    abstract fun getCurrentTimeOfDay(): TimeOfDay?

    // TODO: documentation. Explain this is always corresponding to the local timezone of the device
    //  (which isn't tied to the locale).
    abstract fun parseOppiaDate(dateString: String): OppiaDate?

    // TODO: document that this computes time not considering the locale and should only be used for machine cases (like log statements).
    abstract fun computeCurrentTimeString(): String

    enum class TimeOfDay {
      MORNING,
      AFTERNOON,
      EVENING
    }

    interface OppiaDate {
      fun isBeforeToday(): Boolean
    }
  }

  // TODO: document that this is generally only available via the domain layer.
  abstract class DisplayLocale(override val localeContext: OppiaLocaleContext): OppiaLocale() {
    abstract fun computeDateString(timestampMillis: Long): String

    abstract fun computeTimeString(timestampMillis: Long): String

    abstract fun computeDateTimeString(timestampMillis: Long): String

    abstract fun getLayoutDirection(): Int

    // TODO: mention bidi wrapping (only applied to strings) & machine readable args
    // TODO: document that receiver is the format (unlike String.format()).
    abstract fun String.formatInLocale(vararg args: Any?): String

    abstract fun String.capitalizeForHumans(): String

    abstract fun Resources.getStringInLocale(@StringRes id: Int): String

    abstract fun Resources.getStringInLocale(@StringRes id: Int, vararg formatArgs: Any?): String

    // TODO: for this & others, document that they won't necessarily follow the locale of this object
    //  (they actually depend on the locale specified in Resources).
    abstract fun Resources.getStringArrayInLocale(@ArrayRes id: Int): List<String>

    abstract fun Resources.getQuantityStringInLocale(@PluralsRes id: Int, quantity: Int): String

    abstract fun Resources.getQuantityStringInLocale(
      @PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?
    ): String

    abstract fun Resources.getQuantityTextInLocale(@PluralsRes id: Int, quantity: Int): CharSequence
  }

  data class ContentLocale(override val localeContext: OppiaLocaleContext): OppiaLocale()
}
