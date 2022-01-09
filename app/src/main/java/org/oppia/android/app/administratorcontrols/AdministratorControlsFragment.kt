package org.oppia.android.app.administratorcontrols

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Administrator Controls of the application. */
class AdministratorControlsFragment : InjectableFragment() {
  @Inject
  lateinit var administratorControlsFragmentPresenter: AdministratorControlsFragmentPresenter

  companion object {
    private const val IS_MULTIPANE_ARGUMENT_KEY = "AdministratorControlsFragment.is_multipane"

    /** Creates a new instance of [AdministratorControlsFragment]. */
    fun newInstance(isMultipane: Boolean): AdministratorControlsFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_ARGUMENT_KEY, isMultipane)
      val fragment = AdministratorControlsFragment()
      fragment.arguments = args
      return fragment
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
      checkNotNull(arguments) {
        "Expected arguments to be passed to AdministratorControlsFragment"
      }
    val isMultipane = args.getBoolean(IS_MULTIPANE_ARGUMENT_KEY)
    return administratorControlsFragmentPresenter
      .handleCreateView(inflater, container, isMultipane)
  }

  /** Manually sets the backstack string to a specific fragment. */
  fun setSelectedFragment(selectedFragment: String) {
    administratorControlsFragmentPresenter.setSelectedFragment(selectedFragment)
  }
}
