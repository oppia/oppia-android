package org.oppia.app.walkthrough.topiclist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.model.TopicSummary
import javax.inject.Inject

/** The second slide for [WalkthroughActivity]. */
class WalkthroughTopicListFragment : InjectableFragment(), TopicSummaryClickListener {
  @Inject
  lateinit var walkthroughTopicListFragmentPresenter: WalkthroughTopicListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return walkthroughTopicListFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    walkthroughTopicListFragmentPresenter.changePage(topicSummary)
  }
}
