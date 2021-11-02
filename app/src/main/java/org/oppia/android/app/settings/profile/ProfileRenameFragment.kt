package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

class ProfileRenameFragment : InjectableFragment() {
  @Inject
  lateinit var profileRenameFragmentPresenter: ProfileRenameFragmentPresenter

  companion object {
    //    private const val PROFILE_RENAME_FRAGMENT_DEPENDENCY_INDEX =
//      "ProfileRenameFragment.dependency_index"
//    private const val PROFILE_RENAME_FRAGMENT_PROFILE_INDEX =
//      "ProfileRenameFragment.profile_index"
    private const val PROFILE_ID = "Profile_Id"
    fun newInstance(profileId: Int): ProfileRenameFragment {
      val profileRenameFragment = ProfileRenameFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID, profileId)
      profileRenameFragment.arguments = args
      return profileRenameFragment
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
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to ProfileRenameFragment" }
    val profileId = args.getInt(PROFILE_ID)
    return profileRenameFragmentPresenter.handleCreateView(
      inflater,
      container,
    )
  }
}
