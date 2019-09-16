package org.oppia.app.home.topiclist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that lists all topics available to the user. */
class TopicListFragment : InjectableFragment() {
  @Inject
  lateinit var topicListFragmentPresenter: TopicListFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return topicListFragmentPresenter.handleCreateView(inflater, container)
  }
}
