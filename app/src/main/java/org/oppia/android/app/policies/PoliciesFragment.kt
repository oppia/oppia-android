package org.oppia.android.app.policies

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val POLICIES = "policies"

/** Fragment that contains policies flow of the app. */
class PoliciesFragment : InjectableFragment() {
  @Inject
  lateinit var policiesFragmentPresenter: PoliciesFragmentPresenter

  companion object {
    /** Returns instance of [PoliciesFragment]. */
    fun newInstance(policies: Int): PoliciesFragment {
      val args = Bundle()
      args.putInt(POLICIES, policies)
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
    val policies = args.getInt(POLICIES)
    return policiesFragmentPresenter.handleCreateView(inflater, container, policies)
  }
}
