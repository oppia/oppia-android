package org.oppia.android.app.walkthrough.end

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.WalkthroughFinalFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The final slide for [WalkthroughActivity]. */
class WalkthroughFinalFragment : InjectableFragment() {
  companion object {
    /** Arguments key for WalkthroughFinalFragment. */
    const val WALKTHROUGH_FINAL_FRAGMENT_ARGUMENTS_KEY = "WalkthroughFinalFragment.arguments"

    /** Returns a new [WalkthroughFinalFragment] to display the selected topic corresponding to the specified topic ID. */
    fun newInstance(topicId: String): WalkthroughFinalFragment {
      val args = WalkthroughFinalFragmentArguments.newBuilder().setTopicId(topicId).build()
      return WalkthroughFinalFragment().apply {
        arguments = Bundle().apply {
          putProto(WALKTHROUGH_FINAL_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  @Inject
  lateinit var walkthroughFinalFragmentPresenter: WalkthroughFinalFragmentPresenter

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
        "Expected arguments to be passed to WalkthroughFinalFragment"
      }
    val args = arguments.getProto(
      WALKTHROUGH_FINAL_FRAGMENT_ARGUMENTS_KEY,
      WalkthroughFinalFragmentArguments.getDefaultInstance()
    )
    val topicId =
      checkNotNull(args.topicId) {
        "Expected topicId to be passed to WalkthroughFinalFragment"
      }
    return walkthroughFinalFragmentPresenter.handleCreateView(inflater, container, topicId)
  }
}
