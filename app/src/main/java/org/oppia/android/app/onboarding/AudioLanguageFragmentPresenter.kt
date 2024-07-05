package org.oppia.android.app.onboarding

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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.AudioLanguageSelectionViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val audioLanguageSelectionViewModel: AudioLanguageSelectionViewModel
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding

  /**
   * Returns a newly inflated view to render the fragment with an evaluated audio language as the
   * initial selected language, based on current locale.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View {

    // Hide toolbar as it's not needed in this layout. The toolbar is created by a shared activity
    // and is required in OptionsFragment.
    activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = audioLanguageSelectionViewModel
    }

    audioLanguageSelectionViewModel.setProfileId(profileId)

    audioLanguageSelectionViewModel.setAvailableAudioLanguages()

    audioLanguageSelectionViewModel.languagePreselectionLiveData.observe(
      fragment,
      { selectedLanguage ->
        audioLanguageSelectionViewModel.selectedAudioLanguage.set(selectedLanguage)
      })

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    audioLanguageSelectionViewModel.availableAudioLanguages.observe(fragment, { languages ->
      val adapter = ArrayAdapter(
        fragment.requireContext(),
        R.layout.onboarding_language_dropdown_item,
        R.id.onboarding_language_text_view,
        languages
      )
      binding.audioLanguageDropdownList.setAdapter(adapter)
    })

    binding.audioLanguageDropdownList.apply {
      setRawInputType(EditorInfo.TYPE_NULL)

      onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
          adapter.getItem(position).let {
            if (it != null) {
              // todo update profile audio language
            }
          }
        }
    }

    return binding.root
  }
}
