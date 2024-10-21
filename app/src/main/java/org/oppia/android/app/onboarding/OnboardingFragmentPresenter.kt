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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OnboardingFragmentStateBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.OnboardingAppLanguageSelectionFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val ONBOARDING_FRAGMENT_SAVED_STATE_KEY = "OnboardingFragment.saved_state"

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val translationController: TranslationController,
  private val onboardingAppLanguageViewModel: OnboardingAppLanguageViewModel
) {
  private lateinit var binding: OnboardingAppLanguageSelectionFragmentBinding
  private var profileId: ProfileId = ProfileId.getDefaultInstance()
  private lateinit var selectedLanguage: OppiaLanguage
  private lateinit var supportedLanguages: List<OppiaLanguage>

  /** Handle creation and binding of the [OnboardingFragment] layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, outState: Bundle?): View {
    binding = OnboardingAppLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    val savedSelectedLanguage = outState?.getProto(
      ONBOARDING_FRAGMENT_SAVED_STATE_KEY,
      OnboardingFragmentStateBundle.getDefaultInstance()
    )?.selectedLanguage

    if (savedSelectedLanguage != null) {
      selectedLanguage = savedSelectedLanguage
      onboardingAppLanguageViewModel.setSelectedLanguageLivedata(savedSelectedLanguage)
    } else {
      initializeSelectedLanguageToSystemLanguage()
    }

    retrieveSupportedLanguages()

    subscribeToGetProfileList()

    binding.apply {
      lifecycleOwner = fragment

      onboardingLanguageTitle.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_language_activity_title,
        appLanguageResourceHandler.getStringInLocale(R.string.app_name)
      )

      onboardingAppLanguageViewModel.supportedAppLanguagesList.observe(
        fragment,
        { languagesList ->
          supportedLanguages = languagesList
          val adapter = ArrayAdapter(
            fragment.requireContext(),
            R.layout.onboarding_language_dropdown_item,
            R.id.onboarding_language_text_view,
            languagesList.map { appLanguageResourceHandler.computeLocalizedDisplayName(it) }
          )
          onboardingLanguageDropdown.setAdapter(adapter)
        }
      )

      onboardingAppLanguageViewModel.languageSelectionLiveData.observe(
        fragment,
        { language ->
          selectedLanguage = language
          onboardingLanguageDropdown.setText(
            appLanguageResourceHandler.computeLocalizedDisplayName(
              language
            ),
            false
          )
        }
      )

      onboardingLanguageDropdown.apply {
        setRawInputType(EditorInfo.TYPE_NULL)

        onItemClickListener =
          AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter.getItem(position).let { selectedItem ->
              selectedItem?.let {
                selectedLanguage = supportedLanguages.associateBy { oppiaLanguage ->
                  appLanguageResourceHandler.computeLocalizedDisplayName(oppiaLanguage)
                }[it] ?: OppiaLanguage.ENGLISH
                onboardingAppLanguageViewModel.setSelectedLanguageLivedata(selectedLanguage)
              }
            }
          }
      }

      onboardingLanguageLetsGoButton.setOnClickListener {
        updateSelectedLanguage(selectedLanguage).also {
          val intent =
            OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(activity)
          intent.decorateWithUserProfileId(profileId)
          fragment.startActivity(intent)
        }
      }
    }

    return binding.root
  }

  private val existingProfiles: LiveData<List<Profile>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(),
      ::processGetProfilesResult
    )
  }

  /** Save the current dropdown selection to be retrieved on configuration change. */
  fun saveToSavedInstanceState(outState: Bundle) {
    outState.putProto(
      ONBOARDING_FRAGMENT_SAVED_STATE_KEY,
      OnboardingFragmentStateBundle.newBuilder().setSelectedLanguage(selectedLanguage).build()
    )
  }

  private fun updateSelectedLanguage(selectedLanguage: OppiaLanguage) {
    val selection = AppLanguageSelection.newBuilder().setSelectedLanguage(selectedLanguage).build()
    translationController.updateAppLanguage(profileId, selection).toLiveData()
      .observe(
        fragment,
        { result ->
          when (result) {
            is AsyncResult.Failure -> oppiaLogger.e(
              "OnboardingFragment",
              "Failed to set AppLanguageSelection",
              result.error
            )
            else -> {} // Do nothing. The user should be able to progress regardless of the result.
          }
        }
      )
  }

  private fun initializeSelectedLanguageToSystemLanguage() {
    translationController.getSystemLanguageLocale().toLiveData().observe(
      fragment,
      { result ->
        onboardingAppLanguageViewModel.setSelectedLanguageLivedata(
          processSystemLanguageResult(result)
        )
      }
    )
  }

  private fun processSystemLanguageResult(
    result: AsyncResult<OppiaLocale.DisplayLocale>
  ): OppiaLanguage {
    return when (result) {
      is AsyncResult.Success -> {
        result.value.getCurrentLanguage()
      }
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "OnboardingFragment",
          "Failed to retrieve system language locale.",
          result.error
        )
        OppiaLanguage.ENGLISH
      }
      is AsyncResult.Pending -> OppiaLanguage.ENGLISH
    }
  }

  private fun retrieveSupportedLanguages() {
    translationController.getSupportedAppLanguages().toLiveData().observe(
      fragment,
      { result ->
        when (result) {
          is AsyncResult.Success -> {
            onboardingAppLanguageViewModel.setSupportedAppLanguages(result.value)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "OnboardingFragment",
              "Failed to retrieve supported language list.",
              result.error
            )
          }
          is AsyncResult.Pending -> {}
        }
      }
    )
  }

  private fun subscribeToGetProfileList() {
    existingProfiles.observe(
      fragment,
      { profilesList ->
        if (!profilesList.isNullOrEmpty()) {
          profileId = profilesList.first().id
        } else {
          createDefaultProfile()
        }
      }
    )
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<Profile> {
    val profileList = when (profilesResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "OnboardingFragment", "Failed to retrieve the list of profiles", profilesResult.error
        )
        emptyList()
      }
      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> profilesResult.value
    }

    return profileList
  }

  private fun createDefaultProfile() {
    profileManagementController.addProfile(
      name = "Admin", // TODO(#4938): Refactor to empty name once proper admin profile creation flow
      // is implemented.
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).toLiveData()
      .observe(
        fragment,
        { result ->
          when (result) {
            is AsyncResult.Success -> subscribeToGetProfileList()
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "OnboardingFragment", "Error creating the default profile", result.error
              )
              activity.finish()
            }
            is AsyncResult.Pending -> {}
          }
        }
      )
  }
}
