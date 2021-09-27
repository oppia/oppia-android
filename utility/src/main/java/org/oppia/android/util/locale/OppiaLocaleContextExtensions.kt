package org.oppia.android.util.locale

import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.UNRECOGNIZED
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED

/** Returns the primary [LanguageId] corresponding to this context, based on its intended usage. */
fun OppiaLocaleContext.getLanguageId(): LanguageId {
  return when (usageMode) {
    APP_STRINGS -> languageDefinition.appStringId
    CONTENT_STRINGS -> languageDefinition.contentStringId
    AUDIO_TRANSLATIONS -> languageDefinition.audioTranslationId
    USAGE_MODE_UNSPECIFIED, UNRECOGNIZED, null -> LanguageId.getDefaultInstance()
  }
}

/** Returns the fallback [LanguageId] corresponding to this context, based on its intended usage. */
fun OppiaLocaleContext.getFallbackLanguageId(): LanguageId {
  return when (usageMode) {
    APP_STRINGS -> fallbackLanguageDefinition.appStringId
    CONTENT_STRINGS -> fallbackLanguageDefinition.contentStringId
    AUDIO_TRANSLATIONS -> fallbackLanguageDefinition.audioTranslationId
    USAGE_MODE_UNSPECIFIED, UNRECOGNIZED, null -> LanguageId.getDefaultInstance()
  }
}
