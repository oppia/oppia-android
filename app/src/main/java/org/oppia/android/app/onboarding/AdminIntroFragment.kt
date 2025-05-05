package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AdminIntroFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains the admin intro screen. */
class AdminIntroFragment : InjectableFragment() {

  @Inject
  lateinit var adminIntroFragmentPresenter: AdminIntroFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val profileId = checkNotNull(arguments?.extractCurrentUserProfileId()) {
      "Expected profileId to be included in the arguments for AdminIntroFragment."
    }
    val profileType = checkNotNull(
      arguments?.getProto(
        ADMIN_INTRO_FRAGMENT_ARGS, AdminIntroFragmentArguments.getDefaultInstance()
      )?.profileType
    ) {
      "Expected AdminIntroFragment to have a profileType argument."
    }

    return adminIntroFragmentPresenter.handleCreateView(inflater, container, profileId, profileType)
  }
}
