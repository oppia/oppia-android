package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguageSelection
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
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

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

  /** Handle creation and binding of the [OnboardingFragment] layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingAppLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    createDefaultProfile()

    getSystemLanguage()

    getSupportedLanguages()

    binding.apply {
      lifecycleOwner = fragment

      onboardingLanguageTitle.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_language_activity_title,
        appLanguageResourceHandler.getStringInLocale(R.string.app_name)
      )

      onboardingLanguageLetsGoButton.setOnClickListener {
        val intent =
          OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(activity)
        fragment.startActivity(intent)
      }

      onboardingLanguageLetsGoButton.setOnClickListener {
        val intent =
          OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(activity)
        fragment.startActivity(intent)
      }

      val adapter = ArrayAdapter(
        fragment.requireContext(),
        R.layout.onboarding_language_dropdown_item,
        R.id.onboarding_language_text_view,
        onboardingAppLanguageViewModel.supportedAppLanguagesList
      )

      onboardingLanguageDropdown.apply {

        setAdapter(adapter)

        onboardingAppLanguageViewModel.languageSelectionLiveData.observe(
          fragment,
          { language ->
            setText(
              language,
              false
            )
          }
        )

        setRawInputType(EditorInfo.TYPE_NULL)

        onItemClickListener =
          AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedOppiaLanguage = adapter.getItem(position)
              ?.let { appLanguageResourceHandler.getOppiaLanguageFromDisplayName(it) }
            val selection = AppLanguageSelection.newBuilder().apply {
              selectedLanguage = selectedOppiaLanguage
            }.build()

            translationController.updateAppLanguage(profileId, selection)
            translationController.getAppLanguage(profileId)
          }
      }
    }
    return binding.root
  }

  private fun getSystemLanguage() {
    translationController.getSystemLanguageLocale().toLiveData().observe(
      fragment,
      { result ->
        onboardingAppLanguageViewModel.setSelectedLanguageDisplayName(
          appLanguageResourceHandler.computeLocalizedDisplayName(
            processSystemLanguageResult(result)
          )
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

  private fun getSupportedLanguages() {
    translationController.getSupportedAppLanguages().toLiveData().observe(
      fragment,
      { result ->
        when (result) {
          is AsyncResult.Success -> {
            val supportedLanguages = mutableListOf<String>()
            result.value.map {
              supportedLanguages.add(
                appLanguageResourceHandler.computeLocalizedDisplayName(
                  it
                )
              )
              onboardingAppLanguageViewModel.setSupportedAppLanguages(supportedLanguages)
            }
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

  private fun createDefaultProfile() {
    profileManagementController.addProfile(
      name = "",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    ).toLiveData()
      .observe(
        fragment,
        { result ->
          when (result) {
            is AsyncResult.Success -> retrieveNewProfileId()
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "OnboardingFragment", "Error creating the default profile", result.error
              )
              Profile.getDefaultInstance()
            }
            is AsyncResult.Pending -> {}
          }
        }
      )
  }

  private fun retrieveNewProfileId() {
    profileManagementController.getProfiles().toLiveData().observe(
      fragment,
      { profilesResult ->
        when (profilesResult) {
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "OnboardingFragment",
              "Failed to retrieve the list of profiles",
              profilesResult.error
            )
          }
          is AsyncResult.Pending -> {}
          is AsyncResult.Success -> {
            profileId = profilesResult.value.firstOrNull()?.id ?: ProfileId.getDefaultInstance()
          }
        }
      }
    )
  }
}
