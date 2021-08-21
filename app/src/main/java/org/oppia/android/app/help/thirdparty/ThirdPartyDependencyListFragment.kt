package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val IS_MULTIPANE_KEY = "ThirdPartyDependencyListFragment.is_multipane"

/** Fragment that contains third-party dependency list in the app. */
class ThirdPartyDependencyListFragment : InjectableFragment() {

  @Inject
  lateinit var thirdPartyDependencyListFragmentPresenter: ThirdPartyDependencyListFragmentPresenter

  companion object {

    /** Returns an instance of [ThirdPartyDependencyListFragment]. */
    fun newInstance(isMultipane: Boolean): ThirdPartyDependencyListFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_KEY, isMultipane)
      val fragment = ThirdPartyDependencyListFragment()
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
  ): View {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to ThirdPartyDependencyListFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_KEY, false)
    return thirdPartyDependencyListFragmentPresenter.handleCreateView(
      inflater,
      container,
      isMultipane
    )
  }
}
