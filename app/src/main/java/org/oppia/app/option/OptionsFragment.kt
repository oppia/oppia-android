package org.oppia.app.option

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.oppia.app.R
import org.oppia.app.home.RouteToTopicListener
import org.oppia.app.topic.TopicActivity

class OptionFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)

    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    textSizePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(StoryTextSizeActivity.createStoryTextSizeActivityIntent(requireContext()))
        return true
      }
    }

    val appLanguagePref = findPreference<Preference>(getString(R.string.key_app_language))
    appLanguagePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(AppLanguageActivity.createAppLanguageActivityIntent(requireContext()))
        return true
      }
    }

    val defaultAudioPref = findPreference<Preference>(getString(R.string.key_default_audio))
    defaultAudioPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivity(DefaultAudioActivity.createDefaultAudioActivityIntent(requireContext()))
        return true
      }
    }
  }
}
