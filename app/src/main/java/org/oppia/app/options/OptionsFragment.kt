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
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

const val KEY_MESSAGE_STORY_TEXT_SIZE = "TEXT_SIZE"
const val KEY_MESSAGE_APP_LANGUAGE = "APP_LANGUAGE"
const val KEY_MESSAGE_AUDIO_LANGUAGE = "AUDIO_LANGUAGE"
const val REQUEST_CODE_TEXT_SIZE = 1
const val REQUEST_CODE_APP_LANGUAGE = 2
const val REQUEST_CODE_AUDIO_LANGUAGE= 3

class OptionsFragment @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : PreferenceFragmentCompat() {
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.basic_preference, rootKey)
    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    subscribeToProfileLiveData()
  }

  private fun updateDataIntoUI() {
    val textSizePref = findPreference<Preference>(getString(R.string.key_story_text_size))
    when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> {
        textSizePref!!.summary = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)
      }
      StoryTextSize.MEDIUM_TEXT_SIZE -> {
        textSizePref!!.summary = getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE)
      }
      StoryTextSize.LARGE_TEXT_SIZE -> {
        textSizePref!!.summary = getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE)
      }
      StoryTextSize.EXTRA_LARGE_TEXT_SIZE -> {
        textSizePref!!.summary = getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
    }

    textSizePref!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          StoryTextSizeActivity.createStoryTextSizeActivityIntent(
            requireContext(),
            getString(R.string.key_story_text_size),
            textSizePref!!.summary.toString()
          ), REQUEST_CODE_TEXT_SIZE
        )
        return true
      }
    }

    val appLanguagePref = findPreference<Preference>(getString(R.string.key_app_language))
    appLanguagePref!!.summary = getAppLanguage(appLanguage)
    appLanguagePref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          AppLanguageActivity.createAppLanguageActivityIntent(
            requireContext(),
            getString(R.string.key_app_language),
            appLanguagePref!!.summary.toString()
          ), REQUEST_CODE_APP_LANGUAGE
        )
        return true
      }
    }

    val defaultAudioPref = findPreference<Preference>(getString(R.string.key_default_audio))
    defaultAudioPref!!.summary = getAudioLanguage(audioLanguage)
    defaultAudioPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
      override fun onPreferenceClick(preference: Preference): Boolean {
        startActivityForResult(
          DefaultAudioActivity.createDefaultAudioActivityIntent(
            requireContext(),
            getString(R.string.key_default_audio),
            defaultAudioPref.summary.toString()
          ), REQUEST_CODE_AUDIO_LANGUAGE
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
            getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE) -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.SMALL_TEXT_SIZE)
            }
            getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE) -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
            }
            getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE) -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.LARGE_TEXT_SIZE)
            }
            getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
              profileManagementController.updateStoryTextSize(profileId, StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
            }
          }
        }

        // Update the changed language to summary field.
        preference.key == getString(R.string.key_app_language) -> {
          preference.summary = stringValue
          when (stringValue) {
            getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> profileManagementController.updateAppLanguage(
              profileId,
              AppLanguage.ENGLISH_APP_LANGUAGE
            )
            getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> profileManagementController.updateAppLanguage(
              profileId,
              AppLanguage.HINDI_APP_LANGUAGE
            )
            getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> profileManagementController.updateAppLanguage(
              profileId,
              AppLanguage.CHINESE_APP_LANGUAGE
            )
            getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> profileManagementController.updateAppLanguage(
              profileId,
              AppLanguage.FRENCH_APP_LANGUAGE
            )
          }
        }

        // Update the changed audio language to summary field.
        preference.key == getString(R.string.key_default_audio) -> {
          preference.summary = stringValue
          when (stringValue) {
            getAudioLanguage(AudioLanguage.NO_AUDIO) -> profileManagementController.updateAudioLanguage(
              profileId,
              AudioLanguage.NO_AUDIO
            )
            getAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE) -> profileManagementController.updateAudioLanguage(
              profileId,
              AudioLanguage.ENGLISH_AUDIO_LANGUAGE
            )
            getAudioLanguage(AudioLanguage.HINDI_AUDIO_LANGUAGE) -> profileManagementController.updateAudioLanguage(
              profileId,
              AudioLanguage.HINDI_AUDIO_LANGUAGE
            )
            getAudioLanguage(AudioLanguage.CHINESE_AUDIO_LANGUAGE) -> profileManagementController.updateAudioLanguage(
              profileId,
              AudioLanguage.CHINESE_AUDIO_LANGUAGE
            )
            getAudioLanguage(AudioLanguage.FRENCH_AUDIO_LANGUAGE) -> profileManagementController.updateAudioLanguage(
              profileId,
              AudioLanguage.FRENCH_AUDIO_LANGUAGE
            )
          }
        }
      }
      /* Boolean`= */true
    }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_TEXT_SIZE -> {
        val textSize = data!!.getStringExtra(KEY_MESSAGE_STORY_TEXT_SIZE) as String
        bindPreferenceSummaryToValue(textSize, findPreference(getString(R.string.key_story_text_size)))
      }
      REQUEST_CODE_APP_LANGUAGE -> {
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
      storyTextSize = it.storyTextSize
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

  fun getStoryTextSize(storyTextSize: StoryTextSize): String {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> "Small"
      StoryTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      StoryTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }

  fun getAppLanguage(appLanguage: AppLanguage): String {
    return when (appLanguage) {
      AppLanguage.ENGLISH_APP_LANGUAGE -> "English"
      AppLanguage.HINDI_APP_LANGUAGE -> "Hindi"
      AppLanguage.FRENCH_APP_LANGUAGE -> "French"
      AppLanguage.CHINESE_APP_LANGUAGE -> "Chinese"
      else -> "English"
    }
  }

  fun getAudioLanguage(audioLanguage: AudioLanguage): String {
    return when (audioLanguage) {
      AudioLanguage.NO_AUDIO -> "No Audio"
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> "English"
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> "Hindi"
      AudioLanguage.FRENCH_AUDIO_LANGUAGE -> "French"
      AudioLanguage.CHINESE_AUDIO_LANGUAGE -> "Chinese"
      else -> "No Audio"
    }
  }
}
