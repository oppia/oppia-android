package org.oppia.android.app.walkthrough.end

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val TOPIC_ID_ARGUMENT_KEY = "WalkthroughFinalFragment.topic_id"

/** The final slide for [WalkthroughActivity]. */
class WalkthroughFinalFragment : InjectableFragment() {
  companion object {
    /** Returns a new [WalkthroughFinalFragment] to display the selected topic corresponding to the specified topic ID. */
    fun newInstance(topicId: String): WalkthroughFinalFragment {
      val storyFragment = WalkthroughFinalFragment()
      val args = Bundle()
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      storyFragment.arguments = args
      return storyFragment
    }
  }

  @Inject
  lateinit var walkthroughFinalFragmentPresenter: WalkthroughFinalFragmentPresenter

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
        "Expected arguments to be passed to WalkthroughFinalFragment"
      }
    val topicId =
      checkNotNull(args.getString(TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topicId to be passed to WalkthroughFinalFragment"
      }
    return walkthroughFinalFragmentPresenter.handleCreateView(inflater, container, topicId)
  }
}
