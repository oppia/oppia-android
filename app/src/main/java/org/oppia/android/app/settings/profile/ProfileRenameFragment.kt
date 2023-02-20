package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that provides functionality for renaming the profile name. */
class ProfileRenameFragment : InjectableFragment() {
  @Inject
  lateinit var profileRenameFragmentPresenter: ProfileRenameFragmentPresenter

  companion object {

    /** Returns the instance of [ProfileRenameFragment]. */
    fun newInstance(profileId: ProfileId): ProfileRenameFragment {
      val profileRenameFragment = ProfileRenameFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
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
    val profileId = args.extractCurrentUserProfileId()
    return profileRenameFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId
    )
  }
}
