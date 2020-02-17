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
import org.oppia.app.home.KEY_HOME_PROFILE_ID
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

const val STORY_TEXT_SIZE_SMALL = "Small"
const val STORY_TEXT_SIZE_MEDIUM = "Medium"
const val STORY_TEXT_SIZE_LARGE = "Large"
const val STORY_TEXT_SIZE_EXTRA_LARGE = "Extra Large"

const val SMALL_TEXT_SIZE_VALUE = 16f
const val MEDIUM_TEXT_SIZE_VALUE = 18f
const val LARGE_TEXT_SIZE_VALUE = 20f
const val EXTRA_LARGE_TEXT_SIZE_VALUE = 22f

const val KEY_MESSAGE_STORY_TEXT_SIZE = "Text Size"
const val KEY_MESSAGE_APP_LANGUAGE = "App Language"
const val KEY_MESSAGE_AUDIO_LANGUAGE = "Audio Language"

class OptionsFragment @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : PreferenceFragmentCompat() {
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var storyTextSize = SMALL_TEXT_SIZE_VALUE
  private var appLanguage = "English"
  private var audioLanguage = "No Audio"

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)
    internalProfileId = activity.intent.getIntExtra(KEY_HOME_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    subscribeToProfileLiveData()
  }

  private fun updateDataIntoUI() {
    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    when (storyTextSize) {
      SMALL_TEXT_SIZE_VALUE -> {
        textSizePref!!.summary = STORY_TEXT_SIZE_SMALL
      }
      MEDIUM_TEXT_SIZE_VALUE -> {
        textSizePref!!.summary = STORY_TEXT_SIZE_MEDIUM
      }
      LARGE_TEXT_SIZE_VALUE -> {
        textSizePref!!.summary = STORY_TEXT_SIZE_LARGE
      }
      EXTRA_LARGE_TEXT_SIZE_VALUE -> {
        textSizePref!!.summary = STORY_TEXT_SIZE_EXTRA_LARGE
      }
    }

    textSizePref!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          StoryTextSizeActivity.createStoryTextSizeActivityIntent(
            requireContext(),
            getString(R.string.key_story_text_size),
            textSizePref.summary.toString()
          ), 1
        )
        return true
      }
    }

    val appLanguagePref = findPreference<Preference>(getString(R.string.key_app_language))
    appLanguagePref!!.summary = appLanguage
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
    defaultAudioPref!!.summary = audioLanguage
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

  private fun bindPreferenceSummaryToValue(typeOfValue: String, preference: Preference?) {
    preference!!.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

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
        // Update the changed Text size to summary field.
        preference.key == getString(R.string.key_story_text_size) -> {
          preference.summary = stringValue
          when (stringValue) {
            STORY_TEXT_SIZE_SMALL -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.SMALL_TEXT_SIZE)
            }
            STORY_TEXT_SIZE_MEDIUM -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
            }
            STORY_TEXT_SIZE_LARGE -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.LARGE_TEXT_SIZE)
            }
            STORY_TEXT_SIZE_EXTRA_LARGE -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
            }
          }
        }

        // Update the changed language to summary field.
        preference.key == getString(R.string.key_app_language) -> {
          preference.summary = stringValue
          profileManagementController.updateAppLanguage(profileId, stringValue)
        }

        // Update the changed audio language to summary field.
        preference.key == getString(R.string.key_default_audio) -> {
          preference.summary = stringValue
          profileManagementController.updateAudioLanguage(profileId, stringValue)
        }
      }
      true
    }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      1 -> {
        val textSize = data!!.getStringExtra(KEY_MESSAGE_STORY_TEXT_SIZE) as String
        bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
      }
      2 -> {
        val appLanguage = data!!.getStringExtra(KEY_MESSAGE_APP_LANGUAGE) as String
        bindPreferenceSummaryToValue(appLanguage, findPreference(getString(R.string.key_app_language)))
      }
      else -> {
        val audioLanguage = data!!.getStringExtra(KEY_MESSAGE_AUDIO_LANGUAGE) as String
        bindPreferenceSummaryToValue(audioLanguage, findPreference(getString(R.string.key_default_audio)))
      }
    }

  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(activity, Observer<Profile> {
      storyTextSize = profileManagementController.getStoryTextSize(it.storyTextSize)
      appLanguage = it.appLanguage
      audioLanguage = it.audioLanguage
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
