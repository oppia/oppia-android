package org.oppia.android.app.walkthrough.topiclist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.home.topiclist.TopicSummaryClickListener
import org.oppia.android.app.model.TopicSummary
import javax.inject.Inject

/** The second slide for [WalkthroughActivity]. */
class WalkthroughTopicListFragment : InjectableFragment(), TopicSummaryClickListener {
  @Inject
  lateinit var walkthroughTopicListFragmentPresenter: WalkthroughTopicListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be passed to WalkthroughTopicListFragment."
      }
    val internalProfileId = args.getInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, /* defaultValue = */ -1)
    return walkthroughTopicListFragmentPresenter.handleCreateView(
      inflater, container, internalProfileId
    )
  }

  override fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    walkthroughTopicListFragmentPresenter.changePage(topicSummary)
  }

  /** Dagger injector for [WalkthroughTopicListFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: WalkthroughTopicListFragment)
  }

  companion object {
    private const val KEY_INTERNAL_PROFILE_ID_ARGUMENT =
      "WalkthroughTopicListFragment.internal_profile_id"

    /**
     * Returns a new [WalkthroughTopicListFragment] corresponding to the profile indicated by the
     * specified [internalProfileId].
     */
    fun createFragment(internalProfileId: Int): WalkthroughTopicListFragment {
      return WalkthroughTopicListFragment().apply {
        arguments = Bundle().apply { putInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, internalProfileId) }
      }
    }
  }
}
