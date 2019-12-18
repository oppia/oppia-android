package org.oppia.app.options

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.oppia.app.R
import android.content.SharedPreferences

class OptionsFragment : PreferenceFragmentCompat() {

  private lateinit var sharedPref :SharedPreferences

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)

    sharedPref =  PreferenceManager
      .getDefaultSharedPreferences(requireContext())

    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    textSizePref.summary = sharedPref.getString(getString(R.string.key_story_text_size), "")
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
    appLanguagePref.summary = sharedPref.getString(getString(R.string.key_app_language), "English")
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
    defaultAudioPref.summary = sharedPref.getString(getString(R.string.key_default_audio), "No Audio")
    defaultAudioPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          DefaultAudioActivity.createDefaultAudioActivityIntent(
            requireContext(),
            getString(R.string.key_default_audio),
            defaultAudioPref.summary.toString()
          ), 3
        )
        return true
      }
    }
  }

  private fun bindPreferenceSummaryToValue(typeOfValue: String, preference: Preference) {
    preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

    bindPreferenceSummaryToValueListener.onPreferenceChange(
      preference,sharedPref.getString(preference.key, "")
    )
  }

  private val bindPreferenceSummaryToValueListener =
    Preference.OnPreferenceChangeListener { preference, newValue ->
      val stringValue = newValue.toString()
      when {
        preference.key == getString(R.string.key_story_text_size) -> // update the changed Text size  to summary filed
          preference.summary = stringValue
        preference.key == getString(R.string.key_app_language) -> // update the changed language  to summary filed
          preference.summary = stringValue
        preference.key == getString(R.string.key_default_audio) -> // update the changed audio language  to summary filed
          preference.summary = stringValue
      }
      true
    }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val editor = sharedPref.edit()
    // Check which request we're responding to
    when (requestCode) {
      1 -> {
        val textSize = data!!.getStringExtra("MESSAGE") as String
        editor.putString(getString(R.string.key_story_text_size), textSize)
        editor.commit()
        bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
      }
      2 -> {
        val appLanguage = data!!.getStringExtra("MESSAGE") as String
        editor.putString(getString(R.string.key_app_language), appLanguage)
        editor.commit()
        bindPreferenceSummaryToValue(appLanguage, findPreference(getString(R.string.key_app_language)))
      }
      else -> {
        val audioLanguage = data!!.getStringExtra("MESSAGE") as String
        editor.putString(getString(R.string.key_default_audio), audioLanguage)
        editor.commit()
        bindPreferenceSummaryToValue(audioLanguage, findPreference(getString(R.string.key_default_audio)))
      }
    }

  }
}
