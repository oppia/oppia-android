package org.oppia.android.app.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.AppLanguageSelectionFragmentBinding
import org.oppia.android.app.model.AppLanguageFragmentStateBundle
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OnboardingFragmentStateBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.onboarding.AppLanguageViewModel
import org.oppia.android.app.options.AudioLanguageFragment.Companion.FRAGMENT_SAVED_STATE_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The presenter for [AppLanguageFragment]. */
class AppLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val oppiaLogger: OppiaLogger,
  private val translationController: TranslationController,
  private val appLanguageViewModel: AppLanguageViewModel
) {
  private lateinit var binding: AppLanguageSelectionFragmentBinding
  private var profileId: ProfileId = ProfileId.getDefaultInstance()
  private lateinit var selectedLanguage: OppiaLanguage
  private lateinit var supportedLanguages: List<OppiaLanguage>

  /** Handles the creation and binding of the [AppLanguageFragment] layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    outState: Bundle?,
    currentSelection: OppiaLanguage,
    profileId: ProfileId
  ): View {
    this.profileId = profileId

    binding = AppLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    val savedSelectedLanguage = outState?.getProto(
      FRAGMENT_SAVED_STATE_KEY,
      OnboardingFragmentStateBundle.getDefaultInstance()
    )?.selectedLanguage

    selectedLanguage = savedSelectedLanguage ?: currentSelection

    appLanguageViewModel.setSelectedLanguageLivedata(selectedLanguage)

    retrieveSupportedLanguages()

    binding.apply {
      lifecycleOwner = fragment

      onboardingLanguageLetsGoButton.visibility = View.GONE
      onboardingLanguageTitle.visibility = View.GONE
      onboardingLanguageSubtitle.visibility = View.GONE
      onboardingLanguageText.visibility = View.GONE

      appLanguageViewModel.supportedAppLanguagesList.observe(
        fragment
      ) { languagesList ->
        supportedLanguages = languagesList
        val adapter = ArrayAdapter(
          fragment.requireContext(),
          R.layout.language_dropdown_item,
          R.id.language_text_view,
          languagesList.map { appLanguageResourceHandler.computeLocalizedDisplayName(it) }
        )
        onboardingLanguageDropdown.setAdapter(adapter)
      }

      appLanguageViewModel.selectedLanguageLiveData.observe(
        fragment
      ) { language ->
        onboardingLanguageDropdown.setText(
          appLanguageResourceHandler.computeLocalizedDisplayName(language),
          false
        )
        updateSelectedLanguage(selectedLanguage)
        updateAppLanguage(selectedLanguage)
      }

      onboardingLanguageDropdown.apply {
        setRawInputType(EditorInfo.TYPE_NULL)

        onItemClickListener =
          AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter.getItem(position).let { selectedItem ->
              selectedItem?.let {
                selectedLanguage = supportedLanguages.associateBy { oppiaLanguage ->
                  appLanguageResourceHandler.computeLocalizedDisplayName(oppiaLanguage)
                }[it] ?: OppiaLanguage.ENGLISH
                appLanguageViewModel.setSelectedLanguageLivedata(selectedLanguage)
              }
            }
          }
      }
    }

    return binding.root
  }

  /** Save the current dropdown selection to be retrieved on configuration change. */
  fun saveToSavedInstanceState(outState: Bundle) {
    outState.putProto(
      FRAGMENT_SAVED_STATE_KEY,
      AppLanguageFragmentStateBundle.newBuilder().setOppiaLanguage(selectedLanguage).build()
    )
  }

  private fun retrieveSupportedLanguages() {
    translationController.getSupportedAppLanguages().toLiveData().observe(
      fragment
    ) { result ->
      when (result) {
        is AsyncResult.Success -> {
          appLanguageViewModel.setSupportedAppLanguages(result.value)
        }
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "AppLanguageFragment",
            "Failed to retrieve supported language list.",
            result.error
          )
        }
        is AsyncResult.Pending -> {}
      }
    }
  }

  private fun updateSelectedLanguage(selectedLanguage: OppiaLanguage) {
    val selection = AppLanguageSelection.newBuilder().setSelectedLanguage(selectedLanguage).build()
    translationController.updateAppLanguage(profileId, selection)
  }

  private fun updateAppLanguage(appLanguage: OppiaLanguage) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity -> parentActivity.optionActivityPresenter.updateAppLanguage(appLanguage)
      is AppLanguageActivity -> parentActivity.appLanguageActivityPresenter.setLanguageSelected(
        appLanguage
      )
    }
  }
}
