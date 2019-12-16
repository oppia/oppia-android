package org.oppia.app.option

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.oppia.app.R
import androidx.preference.PreferenceManager

class OptionsFragment : PreferenceFragmentCompat(), OptionSelectorListener {


  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)

    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    textSizePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(
          StoryTextSizeActivity.createStoryTextSizeActivityIntent(
            requireContext(),
            getString(R.string.key_story_text_size)
          )
        )
        return true
      }
    }

    val appLanguagePref = findPreference<Preference>(getString(R.string.key_app_language))
    appLanguagePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(
          AppLanguageActivity.createAppLanguageActivityIntent(
            requireContext(),
            getString(R.string.key_app_language)
          )
        )
        return true
      }
    }

    val defaultAudioPref = findPreference<Preference>(getString(R.string.key_default_audio))
    defaultAudioPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(
          DefaultAudioActivity.createDefaultAudioActivityIntent(
            requireContext(),
            getString(R.string.key_default_audio)
          )
        )
        return true
      }
    }
  }

  private fun bindPreferenceSummaryToValue(typeOfValue: String, preference: Preference) {
    preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

    sBindPreferenceSummaryToValueListener.onPreferenceChange(
      preference,
      PreferenceManager
        .getDefaultSharedPreferences(preference.context)
        .getString(preference.key, typeOfValue)
    )
  }

  private val sBindPreferenceSummaryToValueListener =
    Preference.OnPreferenceChangeListener { preference, newValue ->
      val stringValue = newValue.toString()
      if (preference.key == getString(R.string.key_story_text_size)) {
        // update the changed Text size  to summary filed
        preference.summary = stringValue
      } else if (preference.key == getString(R.string.key_app_language)) {
        // update the changed language  to summary filed
        preference.summary = stringValue
      } else if (preference.key == getString(R.string.key_default_audio)) {
        // update the changed audio language  to summary filed
        preference.summary = stringValue
      }
      true
    }

  override fun storyTextSizeSelected(textSize: String, pref_key: String) {
    bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
  }

  override fun appLanguageSelected(appLanguage: String, pref_key: String) {
    Log.d("interface","=="+appLanguage)
    bindPreferenceSummaryToValue(appLanguage, findPreference(getString(R.string.key_app_language)))
  }

  override fun audioLanguageSelected(audioLanguage: String, pref_key: String) {
    bindPreferenceSummaryToValue(audioLanguage, findPreference(getString(R.string.key_default_audio)))

  }
}
