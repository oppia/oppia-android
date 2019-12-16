package org.oppia.app.option

/** Interface to update the selectedOptions in [OptionActivityPresenter]. */
interface OptionSelectorListener {
  /** This text size will get added to User preferences in [OptionActivityPresenter]. */
  fun storyTextSizeSelected(textSize: String, pref_key: String)

  /** This app language will get added to User preferences in [OptionActivityPresenter]. */
  fun appLanguageSelected(appLanguage: String, pref_key: String)

  /** This audio language will get added to User preferences in [OptionActivityPresenter]. */
  fun audioLanguageSelected(audioLanguage: String, pref_key: String)
}
