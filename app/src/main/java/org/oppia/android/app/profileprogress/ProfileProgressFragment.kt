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
    internal const val PROFILE_PROGRESS_FRAGMENT_PROFILE_ID_KEY =
      "ProfileProgressFragment.internal_profile_id"

    /** Returns a new [ProfileProgressFragment] to display the progress for a specified profile ID. */
    fun newInstance(internalProfileId: Int): ProfileProgressFragment {
      val profileProgressFragment = ProfileProgressFragment()
      val args = Bundle()
      args.putInt(PROFILE_PROGRESS_FRAGMENT_PROFILE_ID_KEY, internalProfileId)
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
    val internalProfileId = args.getInt(PROFILE_PROGRESS_FRAGMENT_PROFILE_ID_KEY, -1)
    return profileProgressFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }

  override fun onProfilePictureClicked() {
    profileProgressFragmentPresenter.showPictureEditDialog()
  }
}
