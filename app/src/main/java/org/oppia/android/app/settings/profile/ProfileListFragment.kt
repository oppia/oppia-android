package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileListFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to display all profiles to admin. */
class ProfileListFragment : InjectableFragment() {
  @Inject
  lateinit var profileListFragmentPresenter: ProfileListFragmentPresenter

  companion object {
    /** Arguments key for ProfileListFragment. */
    const val PROFILE_LIST_FRAGMENT_ARGUMENTS_KEY = "ProfileListFragment.arguments"

    /** This creates the new instance of [ProfileListFragment]. */
    fun newInstance(isMultipane: Boolean = false): ProfileListFragment {
      val args = ProfileListFragmentArguments.newBuilder().setIsMultipane(isMultipane).build()
      return ProfileListFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_LIST_FRAGMENT_ARGUMENTS_KEY, args)
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
      "Expected variables to be passed to ProfileListFragment"
    }
    val args = arguments.getProto(
      PROFILE_LIST_FRAGMENT_ARGUMENTS_KEY,
      ProfileListFragmentArguments.getDefaultInstance()
    )
    val isMultipane = args.isMultipane
    return profileListFragmentPresenter.handleOnCreateView(inflater, container, isMultipane)
  }
}
