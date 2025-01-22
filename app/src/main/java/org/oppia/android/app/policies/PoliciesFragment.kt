package org.oppia.android.app.policies

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.PoliciesFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Argument key for PoliciesFragment. */
const val POLICIES_FRAGMENT_POLICY_PAGE_ARGUMENT_PROTO = "PoliciesFragment.policy_page"

/** Fragment that contains policies flow of the app. */
class PoliciesFragment : InjectableFragment() {
  @Inject
  lateinit var policiesFragmentPresenter: PoliciesFragmentPresenter

  companion object {
    /** Returns instance of [PoliciesFragment]. */
    fun newInstance(policiesFragmentArguments: PoliciesFragmentArguments): PoliciesFragment {
      val args = Bundle()
      args.putProto(POLICIES_FRAGMENT_POLICY_PAGE_ARGUMENT_PROTO, policiesFragmentArguments)
      val fragment = PoliciesFragment()
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
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to PoliciesFragment"
    }
    val policies =
      args.getProto(
        POLICIES_FRAGMENT_POLICY_PAGE_ARGUMENT_PROTO,
        PoliciesFragmentArguments.getDefaultInstance()
      )
    return policiesFragmentPresenter
      .handleCreateView(inflater, container, policies, savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    policiesFragmentPresenter.handleSaveInstanceState(outState)
  }
}
