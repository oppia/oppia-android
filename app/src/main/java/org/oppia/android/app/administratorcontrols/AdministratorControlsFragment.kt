package org.oppia.android.app.administratorcontrols

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AdministratorControlsFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Argument key for Administrator Controls Fragment. */
const val ADMINISTRATOR_CONTROLS_FRAGMENT_ARGUMENTS_KEY = "AdministratorControlsFragment.arguments"

/** Fragment that contains Administrator Controls of the application. */
class AdministratorControlsFragment : InjectableFragment() {
  @Inject
  lateinit var administratorControlsFragmentPresenter: AdministratorControlsFragmentPresenter

  companion object {

    /** Creates a new instance of [AdministratorControlsFragment]. */
    fun newInstance(isMultipane: Boolean): AdministratorControlsFragment {
      val args =
        AdministratorControlsFragmentArguments.newBuilder().setIsMultipane(isMultipane).build()
      return AdministratorControlsFragment().apply {
        arguments = Bundle().apply {
          putProto(ADMINISTRATOR_CONTROLS_FRAGMENT_ARGUMENTS_KEY, args)
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
    val arguments =
      checkNotNull(arguments) {
        "Expected arguments to be passed to AdministratorControlsFragment"
      }
    val args = arguments.getProto(
      ADMINISTRATOR_CONTROLS_FRAGMENT_ARGUMENTS_KEY,
      AdministratorControlsFragmentArguments.getDefaultInstance()
    )
    val isMultipane = args.isMultipane
    return administratorControlsFragmentPresenter
      .handleCreateView(inflater, container, isMultipane)
  }

  /** Manually sets the backstack string to a specific fragment. */
  fun setSelectedFragment(selectedFragment: String) {
    administratorControlsFragmentPresenter.setSelectedFragment(selectedFragment)
  }
}
