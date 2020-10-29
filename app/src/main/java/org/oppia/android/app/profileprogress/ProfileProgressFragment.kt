package org.oppia.android.app.profileprogress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that displays profile progress in the app. */
class ProfileProgressFragment :
  InjectableFragment(),
  ProfilePictureClickListener {
  companion object {
    internal const val INTERNAL_PROFILE_ID_ARGUMENT_KEY =
      "ProfileProgressFragment.internal_profile_id"

    /** Returns a new [ProfileProgressFragment] to display the progress for a specified profile ID. */
    fun newInstance(internalProfileId: Int): ProfileProgressFragment {
      val profileProgressFragment = ProfileProgressFragment()
      val args = Bundle()
      args.putInt(INTERNAL_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      profileProgressFragment.arguments = args
      return profileProgressFragment
    }
  }

  @Inject
  lateinit var profileProgressFragmentPresenter: ProfileProgressFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    ProfileProgressFragment: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to ProfileProgressFragment" }
    val internalProfileId = args.getInt(INTERNAL_PROFILE_ID_ARGUMENT_KEY, -1)
    return profileProgressFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }

  override fun onProfilePictureClicked() {
    profileProgressFragmentPresenter.showPictureEditDialog()
  }
}
