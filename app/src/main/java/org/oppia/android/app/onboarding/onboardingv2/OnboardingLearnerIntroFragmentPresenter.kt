package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.options.LoadAudioLanguageListListener
import org.oppia.android.app.options.RouteToAudioLanguageListListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.OnboardingLearnerIntroFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [OnboardingLearnerIntroFragment]. */
class OnboardingLearnerIntroFragmentPresenter @Inject constructor(
  private var fragment: Fragment,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val oppiaLogger: org.oppia.android.domain.oppialogger.OppiaLogger
) {
  private lateinit var binding: OnboardingLearnerIntroFragmentBinding
  private lateinit var routeToAudioLanguageListListener: RouteToAudioLanguageListListener
  private lateinit var loadAudioLanguageListListener: LoadAudioLanguageListListener

  /** Handle creation and binding of the  OnboardingLearnerIntroFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    fragment: OnboardingLearnerIntroFragment,
    audioLanguage: AudioLanguage
  ): View {
    this.routeToAudioLanguageListListener = fragment
    this.loadAudioLanguageListListener = fragment
    this.fragment = fragment

    binding = OnboardingLearnerIntroFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingNavigationContinue.setOnClickListener {
      routeToAudioLanguageList(audioLanguage)
    }

    observeProfileLivedata(ProfileId.newBuilder().setInternalId(-1).build())

    return binding.root
  }

  private fun routeToAudioLanguageList(audioLanguage: AudioLanguage) {
    routeToAudioLanguageListListener.routeAudioLanguageList(audioLanguage)
  }

  private fun observeProfileLivedata(profileId: ProfileId) {
    profileManagementController.getProfile(profileId).toLiveData().observe(
      fragment,
      { result ->
        when (result) {
          is AsyncResult.Success -> {
            setLearnerName(result.value.name)
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "OnboardingLearnerIntroFragment",
              "Failed to retrieve profile with id $profileId",
              result.error
            )
          }
          is AsyncResult.Pending -> {} // do nothing
        }
      }
    )
  }

  private fun setLearnerName(profileName: String) {
    binding.onboardingLearnerIntroTitle.text =
      appLanguageResourceHandler.getStringInLocaleWithWrapping(
        R.string.onboarding_learner_intro_activity_text, profileName
      )
  }
}
