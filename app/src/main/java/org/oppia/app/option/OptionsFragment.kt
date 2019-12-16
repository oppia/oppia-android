package org.oppia.app.option

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.oppia.app.R

class OptionsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)

    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    textSizePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          StoryTextSizeActivity.createStoryTextSizeActivityIntent(
            requireContext(),
            textSizePref.summary.toString()
          ), 1
        )
        return true
      }
    }

    val appLanguagePref = findPreference<Preference>(getString(R.string.key_app_language))
    appLanguagePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          AppLanguageActivity.createAppLanguageActivityIntent(
            requireContext(),
            getString(R.string.key_app_language),
            appLanguagePref.summary.toString()
          ), 2
        )
        return true
      }
    }

    val defaultAudioPref = findPreference<Preference>(getString(R.string.key_default_audio))
    defaultAudioPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          DefaultAudioActivity.createDefaultAudioActivityIntent(
            requireContext(),
            getString(R.string.key_default_audio),
            defaultAudioPref.summary.toString()
          ),3
        )
        return true
      }
    }
  }

  private fun bindPreferenceSummaryToValue(typeOfValue: String, preference: Preference) {
    preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

    bindPreferenceSummaryToValueListener.onPreferenceChange(
      preference,
      PreferenceManager
        .getDefaultSharedPreferences(preference.context)
        .getString(preference.key, typeOfValue)
    )
  }

  private val bindPreferenceSummaryToValueListener =
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // Check which request we're responding to
    if (requestCode == 1) {
        val textSize = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
    }else if (requestCode == 2){
        val appLanguage = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(appLanguage, findPreference(getString(R.string.key_app_language)))
    }else {
        val audioLanguage = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(audioLanguage, findPreference(getString(R.string.key_default_audio)))
    }
  }

}
