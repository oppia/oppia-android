package org.oppia.android.app.devoptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID

/** Fragment that contains Developer Options of the application. */
class DeveloperOptionsFragment : InjectableFragment() {
  @Inject lateinit var developerOptionsFragmentPresenter: DeveloperOptionsFragmentPresenter

  companion object {
    private const val IS_MULTIPANE_KEY = "IS_MULTIPANE_KEY"
    fun newInstance(isMultipane: Boolean): DeveloperOptionsFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_KEY, isMultipane)
      val fragment = DeveloperOptionsFragment()
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
        "Expected arguments to be passed to DeveloperOptionsFragment"
      }
    val isMultipane = args.getBoolean(IS_MULTIPANE_KEY)
    val internalProfileId = args.getInt(KEY_NAVIGATION_PROFILE_ID)
    return developerOptionsFragmentPresenter
      .handleCreateView(inflater, container, isMultipane)
  }

  fun setSelectedFragment(selectedFragment: String) {
    developerOptionsFragmentPresenter.setSelectedFragment(selectedFragment)
  }
}
