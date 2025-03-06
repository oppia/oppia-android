package org.oppia.android.app.translation

/** Enum representing language selection sources for app localization: system, app-selected, or enforced English. */
enum class LanguageSource {
  /** Indicates the system language (device default) should be used for localization. */
  SYSTEM_LANGUAGE,

  /** Indicates the user-selected app language should be used for localization. */
  APP_LANGUAGE,

  /** Indicates English should be enforced for localization, ignoring system or app settings. */
  ENGLISH
}
