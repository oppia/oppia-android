package org.oppia.android.app.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.Profile
import org.oppia.android.domain.platformparameter.FeatureFlag
import javax.inject.Inject
import javax.inject.Provider

/** Fragment that allows user to select a profile or create new ones. */
class ProfileChooserFragment : InjectableFragment(), RouteToAdminPinListener, ProfileClickListener {
  @Inject
  lateinit var profileChooserFragmentPresenterV1: ProfileChooserFragmentPresenterV1

  @Inject
  lateinit var profileChooserFragmentPresenter: ProfileChooserFragmentPresenter

  @Inject
  @FeatureFlag(FeatureFlagId.ONBOARDING_FLOW_V2)
  lateinit var enableOnboardingFlowV2: Provider<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return if (enableOnboardingFlowV2.get()) {
      profileChooserFragmentPresenter.handleCreateView(inflater, container)
    } else {
      profileChooserFragmentPresenterV1.handleCreateView(inflater, container)
    }
  }

  override fun routeToAdminPin() {
    if (enableOnboardingFlowV2.get()) {
      profileChooserFragmentPresenter.routeToAdminPin()
    } else {
      profileChooserFragmentPresenterV1.routeToAdminPin()
    }
  }

  override fun onProfileClicked(profile: Profile) {
    profileChooserFragmentPresenter.onProfileClick(profile)
  }
}
