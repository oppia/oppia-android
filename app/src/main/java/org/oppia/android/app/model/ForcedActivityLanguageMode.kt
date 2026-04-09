package org.oppia.android.app.model

/** Defines how an activity should resolve its locale. */
enum class ForcedActivityLanguageMode {
  /** Use the user's selected app language, or the system default if no app language is set. */
  USE_APP_LANGUAGE,

  /** Use the system's default language, ignoring the user's app language selection. */
  USE_SYSTEM_LANGUAGE,

  /** Force English locale, regardless of system or app language settings. */
  USE_ENGLISH
}
