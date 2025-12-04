package org.oppia.android.app.onboarding

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.CreateProfileFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment for displaying a new learner profile creation flow. */
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
    createProfileFragmentPresenter.activityResultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        createProfileFragmentPresenter.handleOnActivityResult(result.data)
      }
    }

    val profileId = checkNotNull(arguments?.extractCurrentUserProfileId()) {
      "Expected CreateProfileFragment to have a profileId argument."
    }
    val profileType = checkNotNull(
      arguments?.getProto(
        CREATE_PROFILE_FRAGMENT_ARGS, CreateProfileFragmentArguments.getDefaultInstance()
      )?.profileType
    ) {
      "Expected CreateProfileFragment to have a profileType argument."
    }

    return createProfileFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      profileType
    )
  }
}
