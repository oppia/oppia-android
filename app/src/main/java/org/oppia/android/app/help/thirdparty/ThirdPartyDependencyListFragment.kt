package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ThirdPartyDependencyListFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that contains third-party dependency list in the app. */
class ThirdPartyDependencyListFragment : InjectableFragment() {

  @Inject
  lateinit var thirdPartyDependencyListFragmentPresenter: ThirdPartyDependencyListFragmentPresenter

  companion object {
    /** Arguments key for ThirdPartyDependencyListFragment. */
    private const val THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_ARGUMENTS_KEY =
      "ThirdPartyDependencyListFragment.arguments"

    /** Returns an instance of [ThirdPartyDependencyListFragment]. */
    fun newInstance(isMultipane: Boolean): ThirdPartyDependencyListFragment {
      val args =
        ThirdPartyDependencyListFragmentArguments.newBuilder().setIsMultipane(isMultipane).build()

      return ThirdPartyDependencyListFragment().apply {
        arguments = Bundle().apply {
          putProto(THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_ARGUMENTS_KEY, args)
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
  ): View {
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to ThirdPartyDependencyListFragment"
    }
    val args = arguments.getProto(
      THIRD_PARTY_DEPENDENCY_LIST_FRAGMENT_ARGUMENTS_KEY,
      ThirdPartyDependencyListFragmentArguments.getDefaultInstance()
    )
    val isMultipane = args?.isMultipane ?: false
    return thirdPartyDependencyListFragmentPresenter.handleCreateView(
      inflater,
      container,
      isMultipane
    )
  }
}
