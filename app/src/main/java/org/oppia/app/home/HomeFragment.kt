package org.oppia.app.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.home.topiclist.TopicSummaryClickListener
import org.oppia.app.model.TopicSummary
import javax.inject.Inject

/** Fragment that contains an introduction to the app. */
class HomeFragment : InjectableFragment(), TopicSummaryClickListener {
  @Inject lateinit var homeFragmentPresenter: HomeFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return homeFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onTopicSummaryClicked(topicSummary: TopicSummary) {
    homeFragmentPresenter.onTopicSummaryClicked(topicSummary)
  }
}
