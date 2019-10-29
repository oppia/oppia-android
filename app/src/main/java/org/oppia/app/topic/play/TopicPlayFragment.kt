package org.oppia.app.topic.play

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

internal const val KEY_CURRENT_EXPANDED_LIST_INDEX = "CURRENT_EXPANDED_LIST_INDEX"
internal const val NO_INDEX = -1

/** Fragment that contains subtopic list for play mode. */
class TopicPlayFragment : InjectableFragment(), ExpandedChapterListIndexListener {
  @Inject
  lateinit var topicPlayFragmentPresenter: TopicPlayFragmentPresenter

  private var currentExpandedChapterListIndex = NO_INDEX

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (savedInstanceState != null) {
      currentExpandedChapterListIndex = savedInstanceState.getInt(KEY_CURRENT_EXPANDED_LIST_INDEX)
    }
    return topicPlayFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentExpandedChapterListIndex,
      this as ExpandedChapterListIndexListener
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(KEY_CURRENT_EXPANDED_LIST_INDEX, currentExpandedChapterListIndex)
  }

  override fun onExpandListIconClicked(index: Int) {
    currentExpandedChapterListIndex = index
  }
}
