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
import org.oppia.android.R
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguageFragmentStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AudioLanguageFragment.Companion.FRAGMENT_SAVED_STATE_KEY
import org.oppia.android.app.options.AudioLanguageSelectionViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
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
  private val oppiaLogger: OppiaLogger,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding
  private lateinit var selectedLanguage: String

  /**
   * Returns a newly inflated view to render the fragment with an evaluated audio language as the
   * initial selected language, based on current locale.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    outState: Bundle?
  ): View {
    // Hide toolbar as it's not needed in this layout. The toolbar is created by a shared activity
    // and is required in OptionsFragment.
    activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    val savedSelectedLanguage = outState?.getProto(
      FRAGMENT_SAVED_STATE_KEY,
      AudioLanguageFragmentStateBundle.getDefaultInstance()
    )?.selectedLanguage

    binding.apply {
      lifecycleOwner = fragment
      viewModel = audioLanguageSelectionViewModel
    }

    audioLanguageSelectionViewModel.setProfileId(profileId)

    audioLanguageSelectionViewModel.setAvailableAudioLanguages()

    if (!savedSelectedLanguage.isNullOrBlank()) {
      setSelectedLanguage(savedSelectedLanguage)
    } else {
      observePreselectedLanguage()
    }

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingNavigationBack.setOnClickListener { activity.finish() }

    audioLanguageSelectionViewModel.availableAudioLanguages.observe(
      fragment,
      { languages ->
        val adapter = ArrayAdapter(
          fragment.requireContext(),
          R.layout.onboarding_language_dropdown_item,
          R.id.onboarding_language_text_view,
          languages
        )
        binding.audioLanguageDropdownList.setAdapter(adapter)
      }
    )

    binding.audioLanguageDropdownList.apply {
      setRawInputType(EditorInfo.TYPE_NULL)

      onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
          adapter.getItem(position).let { selectedItem ->
            if (selectedItem != null) {
              selectedLanguage = selectedItem as String
            }
          }
        }
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      updateSelectedAudioLanguage(selectedLanguage, profileId)
    }

    return binding.root
  }

  private fun observePreselectedLanguage() {
    audioLanguageSelectionViewModel.languagePreselectionLiveData.observe(
      fragment,
      { selectedLanguage -> setSelectedLanguage(selectedLanguage) }
    )
  }

  private fun setSelectedLanguage(selectedLanguage: String) {
    this.selectedLanguage = selectedLanguage
    audioLanguageSelectionViewModel.selectedAudioLanguage.set(selectedLanguage)
  }

  private fun updateSelectedAudioLanguage(selectedLanguage: String, profileId: ProfileId) {
    val audioLanguage =
      appLanguageResourceHandler.getAudioLanguageFromLocalizedName(selectedLanguage)
    profileManagementController.updateAudioLanguage(profileId, audioLanguage).toLiveData()
      .observe(fragment) {
        when (it) {
          is AsyncResult.Success -> {
            loginToProfile(profileId)
            val intent = HomeActivity.createHomeActivity(fragment.requireContext(), profileId)
            fragment.startActivity(intent)
            // Finish this activity as well as all activities immediately below it in the current
            // task so that the user cannot navigate back to the onboarding flow by pressing the
            // back button once onboarding is complete
            fragment.activity?.finishAffinity()
          }
          is AsyncResult.Failure ->
            oppiaLogger.e(
              "OnboardingAudioLanguageFragment",
              "Failed to set the selected language.",
              it.error
            )
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun loginToProfile(profileId: ProfileId) {
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
