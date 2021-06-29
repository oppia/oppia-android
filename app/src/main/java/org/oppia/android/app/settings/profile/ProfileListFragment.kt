package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val IS_MULTIPANE_ARGUMENT_KEY = "ProfileListFragment.is_multipane"

/** Fragment to display all profiles to admin. */
class ProfileListFragment : InjectableFragment() {
  @Inject
  lateinit var profileListFragmentPresenter: ProfileListFragmentPresenter

  companion object {
    fun newInstance(isMultipane: Boolean = false): ProfileListFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_ARGUMENT_KEY, isMultipane)
      val fragment = ProfileListFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileListFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_ARGUMENT_KEY)
    return profileListFragmentPresenter.handleOnCreateView(inflater, container, isMultipane)
  }
}
