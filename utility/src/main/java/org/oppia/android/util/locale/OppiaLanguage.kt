package org.oppia.android.util.locale

import org.oppia.android.app.model.SupportedLocale
import java.util.Locale

sealed class OppiaLanguage {
  abstract val supportedLocale: SupportedLocale
  abstract val displayLanguage: String

  sealed class LocaleBasedLanguage(
    override val supportedLocale: SupportedLocale,
    private val locale: Locale
  ): OppiaLanguage() {
    override val displayLanguage: String
      get() = locale.displayLanguage.capitalize(locale)

    object English: LocaleBasedLanguage(SupportedLocale.ENGLISH, Locale.US)
    object Spanish: LocaleBasedLanguage(SupportedLocale.SPANISH, Locale("es", "ES"))
    object Hindi: LocaleBasedLanguage(SupportedLocale.HINDI, Locale("hi", "HI"))
  }

  sealed class MacaronicLanguage(
    override val supportedLocale: SupportedLocale,
    override val displayLanguage: String
  ): OppiaLanguage() {
    object Hinglish: MacaronicLanguage(
      supportedLocale = SupportedLocale.HINGLISH,
      displayLanguage = "Hinglish"
    )
  }

  companion object {
    fun fromSupportedLocale(supportedLocale: SupportedLocale): OppiaLanguage? {
      return when (supportedLocale) {
        SupportedLocale.ENGLISH -> LocaleBasedLanguage.English
        SupportedLocale.SPANISH -> LocaleBasedLanguage.Spanish
        SupportedLocale.HINDI -> LocaleBasedLanguage.Hindi
        SupportedLocale.HINGLISH -> MacaronicLanguage.Hinglish
        SupportedLocale.SUPPORTED_LOCALE_UNSPECIFIED, SupportedLocale.UNRECOGNIZED -> null
      }
    }

    fun fromLanguageCode(languageCode: String): OppiaLanguage? {
      return when (languageCode.toLowerCase(Locale.getDefault())) {
        "en" -> fromSupportedLocale(SupportedLocale.ENGLISH)
        "es" -> fromSupportedLocale(SupportedLocale.SPANISH)
        "hi" -> fromSupportedLocale(SupportedLocale.HINDI)
        "hi_en" -> fromSupportedLocale(SupportedLocale.HINGLISH)
        else -> fromSupportedLocale(SupportedLocale.SUPPORTED_LOCALE_UNSPECIFIED)
      }
    }
  }
}
