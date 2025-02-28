package org.oppia.android.app.translation

/**
 * Represents the possible sources of language selection for localization in the app.
 * This enum is used to determine whether the app should use the system language,
 * the user-selected app language, or enforce English as the language for specific activities.
 */
enum class LanguageSource {
  /**
   * Indicates that the system language (device default) should be used for localization.
   */
  SYSTEM_LANGUAGE,

  /**
   * Indicates that the app-specific language selected by the user should be used for localization.
   */
  APP_LANGUAGE,

  /**
   * Indicates that English should be enforced as the language for localization,
   * regardless of system or app settings.
   */
  ENGLISH
}
