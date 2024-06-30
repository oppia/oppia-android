package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileResetPinFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that resets the profile pin of the user. */
class ProfileResetPinFragment : InjectableFragment() {
  @Inject
  lateinit var profileResetPinFragmentPresenter: ProfileResetPinFragmentPresenter

  companion object {
    /** Arguments key for ProfileResetPinFragment. */
    const val PROFILE_RESET_PIN_FRAGMENT_ARGUMENTS_KEY = "ProfileResetPinFragment.arguments"

    /** Returns instance of [ProfileResetPinFragment]. */
    fun newInstance(
      profileResetPinProfileId: Int,
      profileResetPinIsAdmin: Boolean,
    ): ProfileResetPinFragment {
      val args = ProfileResetPinFragmentArguments.newBuilder().apply {
        this.internalProfileId = profileResetPinProfileId
        this.isAdmin = profileResetPinIsAdmin
      }.build()

      return ProfileResetPinFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_RESET_PIN_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

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
      "Expected arguments to be passed to ProfileResetPinFragment"
    }

    val args =
      arguments.getProto(
        PROFILE_RESET_PIN_FRAGMENT_ARGUMENTS_KEY,
        ProfileResetPinFragmentArguments.getDefaultInstance()
      )
    val profileResetPinProfileId = args.internalProfileId
    val profileResetPinIsAdmin = args.isAdmin

    return profileResetPinFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileResetPinProfileId,
      profileResetPinIsAdmin
    )
  }
}
