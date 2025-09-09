package org.oppia.android.app.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileChooserFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that allows user to select a profile or create new ones. */
class ProfileChooserFragment : InjectableFragment(), RouteToAdminPinListener, ProfileClickListener {
  @Inject
  lateinit var profileChooserFragmentPresenterV1: ProfileChooserFragmentPresenterV1

  @Inject
  lateinit var profileChooserFragmentPresenter: ProfileChooserFragmentPresenter

  @Inject
  @field:EnableOnboardingFlowV2
  lateinit var enableOnboardingFlowV2: PlatformParameterValue<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (enableOnboardingFlowV2.value) {
      val adminProfileId =
        checkNotNull(arguments?.extractCurrentUserProfileId()) {
          "Expected profileId to be included in the arguments for ProfileChooserFragment."
        }

      val profileType = checkNotNull(
        arguments?.getProto(
          PROFILE_CHOOSER_ARGUMENTS_KEY,
          ProfileChooserFragmentArguments.getDefaultInstance()
        )?.profileType
      ) { "Expected ProfileChooserFragment to have a profileType argument." }

      return profileChooserFragmentPresenter.handleCreateView(
        inflater,
        container,
        adminProfileId,
        profileType
      )
    } else {
      return profileChooserFragmentPresenterV1.handleCreateView(inflater, container)
    }
  }

  override fun routeToAdminPin() {
    if (enableOnboardingFlowV2.value) {
      profileChooserFragmentPresenter.routeToAdminPin()
    } else {
      profileChooserFragmentPresenterV1.routeToAdminPin()
    }
  }

  override fun onProfileClicked(profile: Profile) {
    profileChooserFragmentPresenter.onProfileClick(profile)
  }
}
