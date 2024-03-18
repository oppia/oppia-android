package org.oppia.android.app.onboarding.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import org.oppia.android.R
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.AudioLanguageSelectionFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment] V2. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var binding: AudioLanguageSelectionFragmentBinding
  private val orientation = Resources.getSystem().configuration.orientation

  /**
   * Returns a newly inflated view to render the fragment with the specified [audioLanguage] as the
   * initial selected language.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View {

    activity.findViewById<AppBarLayout>(R.id.reading_list_app_bar_layout).visibility = View.GONE

    binding = AudioLanguageSelectionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment

    binding.audioLanguageText.text = appLanguageResourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_language_fragment_text,
      appLanguageResourceHandler.getStringInLocale(R.string.app_name)
    )

    binding.audioLanguageDropdown.adapter = ArrayAdapter(
      fragment.requireContext(),
      R.layout.onboarding_language_dropdown_item,
      R.id.onboarding_language_text_view,
      getAudioLanguageList()
    )

    val currentUserProfileId = retrieveNewProfileId()

    binding.onboardingNavigationContinue.setOnClickListener {
      val intent =
        HomeActivity.createHomeActivity(fragment.requireContext(), currentUserProfileId.internalId)
      fragment.startActivity(intent)
      fragment.activity?.finish()
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingStepsCount?.visibility =
      if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE

    return binding.root
  }

  private fun retrieveNewProfileId(): ProfileId {
    var profileId: ProfileId = ProfileId.getDefaultInstance()
    profileManagementController.getProfiles().toLiveData().observe(
      fragment,
      { profilesResult ->
        when (profilesResult) {
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "AudioLanguageFragmentPresenter",
              "Failed to retrieve the list of profiles",
              profilesResult.error
            )
          }
          is AsyncResult.Pending -> {}
          is AsyncResult.Success -> {
            val sortedProfileList = profilesResult.value.sortedBy { it.id.internalId }
            profileId = sortedProfileList.last().id
          }
        }
      }
    )
    return profileId
  }

  private fun getAudioLanguageList(): List<String> {
    return AudioLanguage.values()
      .filter { it.isValid() }
      .map { audioLanguage ->
        appLanguageResourceHandler.computeLocalizedDisplayName(audioLanguage)
      }
  }

  private fun AudioLanguage.isValid(): Boolean {
    return when (this) {
      AudioLanguage.UNRECOGNIZED, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED,
      AudioLanguage.NO_AUDIO -> false
      else -> true
    }
  }
}
