package org.oppia.android.app.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl

private const val IS_MULTIPANE_KEY = "HelpFragment.bool_is_multipane"

/** Fragment that contains help in the app. */
class HelpFragment : InjectableFragment() {
  @Inject
  lateinit var helpFragmentPresenter: HelpFragmentPresenter

  companion object {
    /** Returns instance of [HelpFragment]. */
    fun newInstance(isMultipane: Boolean): HelpFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_KEY, isMultipane)
      val fragment = HelpFragment()
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
      "Expected arguments to be passed to HelpFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_KEY)
    return helpFragmentPresenter.handleCreateView(inflater, container, isMultipane)
  }
}
