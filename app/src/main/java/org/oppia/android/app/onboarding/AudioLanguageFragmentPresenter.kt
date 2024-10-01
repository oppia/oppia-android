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
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguageFragmentStateBundle
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AudioLanguageFragment.Companion.FRAGMENT_SAVED_STATE_KEY
import org.oppia.android.app.options.AudioLanguageSelectionViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val audioLanguageSelectionViewModel: AudioLanguageSelectionViewModel,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding
  private lateinit var selectedLanguage: OppiaLanguage
  private lateinit var supportedLanguages: List<OppiaLanguage>

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
        supportedLanguages = languages
        val adapter = ArrayAdapter(
          fragment.requireContext(),
          R.layout.onboarding_language_dropdown_item,
          R.id.onboarding_language_text_view,
          languages.map { appLanguageResourceHandler.computeLocalizedDisplayName(it) }
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
        }
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      updateSelectedAudioLanguage(selectedLanguage, profileId).also {
        val intent = HomeActivity.createHomeActivity(fragment.requireContext(), profileId)
        fragment.startActivity(intent)
        // Finish this activity as well as all activities immediately below it in the current
        // task so that the user cannot navigate back to the onboarding flow by pressing the
        // back button once onboarding is complete
        fragment.activity?.finishAffinity()
      }
    }

    return binding.root
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
      .toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Failure ->
            oppiaLogger.e(
              "AudioLanguageFragment",
              "Failed to set the selected language.",
              it.error
            )
          else -> {} // Do nothing.
        }
      }
  }

  /** Save the current dropdown selection to be retrieved on configuration change. */
  fun handleSavedState(outState: Bundle) {
    outState.putProto(
      FRAGMENT_SAVED_STATE_KEY,
      AudioLanguageFragmentStateBundle.newBuilder().setSelectedLanguage(selectedLanguage).build()
    )
  }
}
