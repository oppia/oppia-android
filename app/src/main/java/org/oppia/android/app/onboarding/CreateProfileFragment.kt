package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.CreateProfileFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment argument key for [CreateProfileFragment]. */
const val CREATE_PROFILE_ARGUMENTS_KEY = "CreateProfileFragment.arguments"

/** Fragment that contains the profile creation screen. */
class CreateProfileFragment : InjectableFragment() {
  @Inject
  lateinit var createProfileFragmentPresenter: CreateProfileFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to CreateProfileFragment"
    }
    val profileId = arguments.extractCurrentUserProfileId()
    val fragmentArguments = arguments.getProto(
      CREATE_PROFILE_ARGUMENTS_KEY,
      CreateProfileFragmentArguments.getDefaultInstance()
    )

    return createProfileFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      fragmentArguments.profileType,
      fragmentArguments.avatarColor
    )
  }
}
