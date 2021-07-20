package org.oppia.android.util.extensions

import org.oppia.android.app.model.AppLanguage
import org.oppia.android.app.model.AudioLanguage

// Extension functions for language codes that convert between the different types of language codes
// and their String representations.

enum class LanguageCode {
  UNKNOWN_LANGUAGE_CODE,
  NO_SPECIFIED_LANGUAGE,
  ENGLISH,
  HINDI,
  HINGLISH,
  FRENCH,
  CHINESE;
}

fun LanguageCode.toLanguageCodeString(): String {
  return when (this) {
    LanguageCode.ENGLISH -> "en"
    LanguageCode.HINDI -> "hi"
    LanguageCode.HINGLISH -> "hi-en"
    LanguageCode.FRENCH -> "fr"
    LanguageCode.CHINESE -> "zh"
    else -> "unknown"
  }
}

fun String.toLanguageCode(): LanguageCode {
  return when (this.toLowerCase().trim()) {
    "english", "en", "en_us" -> LanguageCode.ENGLISH
    "hindi", "hi" -> LanguageCode.HINDI
    "hinglish", "hi-en" -> LanguageCode.HINGLISH
    "french", "fr" -> LanguageCode.FRENCH
    "chinese", "zh" -> LanguageCode.CHINESE
    else -> LanguageCode.UNKNOWN_LANGUAGE_CODE
  }
}

fun AppLanguage.toLanguageCode(): LanguageCode {
  return when (this) {
    AppLanguage.ENGLISH_APP_LANGUAGE -> LanguageCode.ENGLISH
    AppLanguage.HINDI_APP_LANGUAGE -> LanguageCode.HINDI
    AppLanguage.FRENCH_APP_LANGUAGE -> LanguageCode.FRENCH
    AppLanguage.CHINESE_APP_LANGUAGE -> LanguageCode.FRENCH
    else -> LanguageCode.UNKNOWN_LANGUAGE_CODE
  }
}

fun AudioLanguage.toLanguageCode(): LanguageCode {
  return when (this) {
    AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> LanguageCode.ENGLISH
    AudioLanguage.HINDI_AUDIO_LANGUAGE -> LanguageCode.HINDI
    AudioLanguage.FRENCH_AUDIO_LANGUAGE -> LanguageCode.FRENCH
    AudioLanguage.CHINESE_AUDIO_LANGUAGE -> LanguageCode.FRENCH
    AudioLanguage.NO_AUDIO -> LanguageCode.NO_SPECIFIED_LANGUAGE
    else -> LanguageCode.UNKNOWN_LANGUAGE_CODE
  }
}
