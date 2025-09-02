package org.oppia.android.app.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.databinding.databinding.AudioLanguageSelectionFragmentBinding
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityParams.ParentScreen
import org.oppia.android.app.model.AudioLanguageFragmentStateBundle
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AudioLanguageActivity
import org.oppia.android.app.options.AudioLanguageFragment.Companion.FRAGMENT_SAVED_STATE_KEY
import org.oppia.android.app.options.AudioLanguageSelectionViewModel
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val audioLanguageSelectionViewModel: AudioLanguageSelectionViewModel,
  private val profileManagementController: ProfileManagementController,
  private val translationController: TranslationController,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding
  private lateinit var selectedLanguage: OppiaLanguage
  private lateinit var supportedLanguages: List<OppiaLanguage>
  private lateinit var parentScreen: ParentScreen

  /**
   * Returns a newly inflated view to render the fragment with an evaluated audio language as the
   * initial selected language, based on current locale.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    outState: Bundle?,
    parentScreen: ParentScreen
  ): View {
    this.parentScreen = parentScreen

    // Hide toolbar as it's not needed in the onboarding layout. The toolbar is created by a shared
    // activity and is required in OptionsFragment.
    if (parentScreen == ParentScreen.LEARNER_INTRO_SCREEN) {
      activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE
    }

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    hideNavigationViews(parentScreen)

    val savedSelectedLanguage = outState?.getProto(
      FRAGMENT_SAVED_STATE_KEY,
      AudioLanguageFragmentStateBundle.getDefaultInstance()
    )?.selectedLanguage

    binding.apply {
      lifecycleOwner = fragment
      viewModel = audioLanguageSelectionViewModel
    }

    audioLanguageSelectionViewModel.updateProfileId(profileId)

    savedSelectedLanguage?.let {
      if (it != OppiaLanguage.LANGUAGE_UNSPECIFIED) {
        setSelectedLanguage(it)
      } else {
        observePreselectedLanguage()
      }
    } ?: observePreselectedLanguage()

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }

    audioLanguageSelectionViewModel.supportedOppiaLanguagesLiveData.observe(
      fragment,
      { languages ->
        val supportedAudioLanguages = languages.filterUnsupportedAudioLanguages()
        supportedLanguages = supportedAudioLanguages
        val adapter = ArrayAdapter(
          fragment.requireContext(),
          R.layout.language_dropdown_item,
          R.id.language_text_view,
          supportedAudioLanguages.map { appLanguageResourceHandler.computeLocalizedDisplayName(it) }
        )
        binding.audioLanguageDropdownList.setAdapter(adapter)
      }
    )

    binding.audioLanguageDropdownList.apply {
      setRawInputType(EditorInfo.TYPE_NULL)

      onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
          val selectedItem = adapter.getItem(position) as? String
          selectedItem?.let {
            selectedLanguage = supportedLanguages.associateBy { oppiaLanguage ->
              appLanguageResourceHandler.computeLocalizedDisplayName(oppiaLanguage)
            }[it] ?: OppiaLanguage.ENGLISH
          }
          setSelectedLanguage(selectedLanguage)
          updateSelectedAudioLanguage(selectedLanguage, profileId)
        }
    }

    binding.onboardingNavigationContinue.setOnClickListener { logInToProfile(profileId) }

    return binding.root
  }

  private fun List<OppiaLanguage>.filterUnsupportedAudioLanguages(): List<OppiaLanguage> {
    return this.filter { language ->
      when (language) {
        OppiaLanguage.UNRECOGNIZED,
        OppiaLanguage.LANGUAGE_UNSPECIFIED,
        OppiaLanguage.HINGLISH,
        OppiaLanguage.PORTUGUESE,
        OppiaLanguage.SWAHILI -> false
        else -> true
      }
    }
  }

  private fun observePreselectedLanguage() {
    audioLanguageSelectionViewModel.languagePreselectionLiveData.observe(
      fragment,
      { selectedLanguage -> setSelectedLanguage(selectedLanguage) }
    )
  }

  private fun setSelectedLanguage(selectedLanguage: OppiaLanguage) {
    this.selectedLanguage = selectedLanguage
    audioLanguageSelectionViewModel.selectedAudioLanguage.set(selectedLanguage)
  }

  private fun updateSelectedAudioLanguage(selectedLanguage: OppiaLanguage, profileId: ProfileId) {
    val audioLanguageSelection =
      AudioTranslationLanguageSelection.newBuilder().setSelectedLanguage(selectedLanguage).build()
    translationController.updateAudioTranslationContentLanguage(profileId, audioLanguageSelection)
      .toLiveData().observe(fragment) { result ->
        when (result) {
          is AsyncResult.Success -> {
            if (parentScreen == ParentScreen.OPTIONS_SCREEN) {
              updateAudioLanguage(getAudioLanguageFromOppiaLanguage(selectedLanguage))
            }
          }
          is AsyncResult.Failure ->
            oppiaLogger.e(
              "AudioLanguageFragment",
              "Failed to set the selected language.",
              result.error
            )
          is AsyncResult.Pending -> {} // Do nothing.
        }
      }
  }

  private fun updateAudioLanguage(audioLanguage: AudioLanguage) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity ->
        parentActivity.optionActivityPresenter.updateAudioLanguage(audioLanguage)
      is AudioLanguageActivity ->
        parentActivity.audioLanguageActivityPresenter.setLanguageSelected(audioLanguage)
    }
  }

  private fun getAudioLanguageFromOppiaLanguage(oppiaLanguage: OppiaLanguage): AudioLanguage {
    return when (oppiaLanguage) {
      OppiaLanguage.UNRECOGNIZED, OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.HINGLISH,
      OppiaLanguage.PORTUGUESE, OppiaLanguage.SWAHILI -> AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
      OppiaLanguage.ARABIC -> AudioLanguage.ARABIC_LANGUAGE
      OppiaLanguage.ENGLISH -> AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      OppiaLanguage.HINDI -> AudioLanguage.HINDI_AUDIO_LANGUAGE
      OppiaLanguage.BRAZILIAN_PORTUGUESE -> AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
      OppiaLanguage.NIGERIAN_PIDGIN -> AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
    }
  }

  private fun hideNavigationViews(parentScreen: ParentScreen) {
    if (parentScreen == ParentScreen.OPTIONS_SCREEN) {
      binding.onboardingStepsCount?.visibility = View.GONE
      binding.onboardingNavigationBack.visibility = View.GONE
      binding.onboardingNavigationContinue.visibility = View.GONE
    }
  }

  private fun logInToProfile(profileId: ProfileId) {
    profileManagementController.loginToProfile(profileId).toLiveData().observe(
      fragment,
      { result ->
        if (result is AsyncResult.Success) {
          navigateToHomeScreen(profileId)
        }
      }
    )
  }

  private fun navigateToHomeScreen(profileId: ProfileId) {
    val intent = if (enableMultipleClassrooms.value) {
      ClassroomListActivity.createClassroomListActivity(fragment.requireContext(), profileId)
    } else {
      HomeActivity.createHomeActivity(fragment.requireContext(), profileId)
    }
    fragment.startActivity(intent)
    // Finish this activity as well as all activities immediately below it in the current
    // task so that the user cannot navigate back to the onboarding flow by pressing the
    // back button once onboarding is complete.
    fragment.activity?.finishAffinity()
  }

  /** Save the current dropdown selection to be retrieved on configuration change. */
  fun handleSavedState(outState: Bundle) {
    outState.putProto(
      FRAGMENT_SAVED_STATE_KEY,
      AudioLanguageFragmentStateBundle.newBuilder().setSelectedLanguage(selectedLanguage).build()
    )
  }
}
