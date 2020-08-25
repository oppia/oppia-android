package org.oppia.app.administratorcontrols

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Administrator Controls of the application. */
class AdministratorControlsFragment : InjectableFragment() {
  @Inject
  lateinit var administratorControlsFragmentPresenter: AdministratorControlsFragmentPresenter

  companion object {
    private const val IS_MULTIPANE_KEY = "IS_MULTIPANE_KEY"
    fun newInstance(isMultipane: Boolean): AdministratorControlsFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_KEY, isMultipane)
      val fragment = AdministratorControlsFragment()
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
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be passed to AdministratorControlsFragment"
      }
    val isMultipane = args.getBoolean(IS_MULTIPANE_KEY)
    return administratorControlsFragmentPresenter
      .handleCreateView(inflater, container, isMultipane)
  }
}
