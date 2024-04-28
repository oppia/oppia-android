package org.oppia.android.app.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.HelpFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that contains help in the app. */
class HelpFragment : InjectableFragment() {
  @Inject
  lateinit var helpFragmentPresenter: HelpFragmentPresenter

  companion object {
    private const val HELP_FRAGMENT_ARGUMENTS_KEY =
      "HelpFragment.arguments"

    /** Returns instance of [HelpFragment]. */
    fun newInstance(isMultipane: Boolean): HelpFragment {
      val args = HelpFragmentArguments.newBuilder().setIsMultipane(isMultipane).build()
      return HelpFragment().apply {
        arguments = Bundle().apply { putProto(HELP_FRAGMENT_ARGUMENTS_KEY, args) }
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
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to HelpFragment"
    }
    val args =
      arguments.getProto(HELP_FRAGMENT_ARGUMENTS_KEY, HelpFragmentArguments.getDefaultInstance())
    val isMultipane = args.isMultipane
    return helpFragmentPresenter.handleCreateView(inflater, container, isMultipane)
  }
}
