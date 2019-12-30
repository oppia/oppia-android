package org.oppia.app.options

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.oppia.app.R
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

class OptionsFragment @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : PreferenceFragmentCompat() {
  private lateinit var profileId: ProfileId
  var storyTextSize = 16f
  var appLanguage = "English"
  var audioLanguage = "No Audio"

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)
    profileId = ProfileId.newBuilder().setInternalId(1).build()
    subscribeToProfileLiveData()
  }

  private fun updateDataIntoUI() {
    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    when (storyTextSize) {
      16f -> {
        textSizePref.summary = "Small"
      }
      18f -> {
        textSizePref.summary = "Medium"
      }
      20f -> {
        textSizePref.summary = "Large"
      }
      22f -> {
        textSizePref.summary = "Extra Large"
      }
    }

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
    appLanguagePref.summary = appLanguage
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
    defaultAudioPref.summary = audioLanguage
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
      preference, PreferenceManager
        .getDefaultSharedPreferences(preference.context)
        .getString(preference.key, typeOfValue)
    )
  }

  private val bindPreferenceSummaryToValueListener =
    Preference.OnPreferenceChangeListener { preference, newValue ->
      val stringValue = newValue.toString()
      when {
        preference.key == getString(R.string.key_story_text_size) -> // update the changed Text size  to summary filed
        {
          preference.summary = stringValue
          when (stringValue) {
            "Small" -> {
              profileManagementController.updateStoryTextSize(profileId, 16f)
            }
            "Medium" -> {
              profileManagementController.updateStoryTextSize(profileId, 18f)
            }
            "Large" -> {
              profileManagementController.updateStoryTextSize(profileId, 20f)
            }
            "Extra Large" -> {
              profileManagementController.updateStoryTextSize(profileId, 22f)
            }
          }
        }
        preference.key == getString(R.string.key_app_language) -> // update the changed language  to summary filed
        {
          preference.summary = stringValue
          profileManagementController.updateAppLanguage(profileId, stringValue)
        }
        preference.key == getString(R.string.key_default_audio) -> // update the changed audio language  to summary filed
        {
          preference.summary = stringValue
          profileManagementController.updateAudioLanguage(profileId, stringValue)
        }
      }
      true
    }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // Check which request we're responding to
    when (requestCode) {
      1 -> {
        val textSize = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
      }
      2 -> {
        val appLanguage = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(appLanguage, findPreference(getString(R.string.key_app_language)))
      }
      else -> {
        val audioLanguage = data!!.getStringExtra("MESSAGE") as String
        bindPreferenceSummaryToValue(audioLanguage, findPreference(getString(R.string.key_default_audio)))
      }
    }

  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(activity, Observer<Profile> { result ->
      storyTextSize = result.storyTextSize
      appLanguage = result.appLanguage
      audioLanguage = result.audioLanguage

      updateDataIntoUI()
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("OptionsFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }
}
