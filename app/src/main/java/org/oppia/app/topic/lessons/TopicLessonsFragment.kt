package org.oppia.app.topic.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.StorySummary
import javax.inject.Inject

private const val KEY_CURRENT_EXPANDED_LIST_INDEX = "CURRENT_EXPANDED_LIST_INDEX"

/** Fragment that contains subtopic list for lessons mode. */
class TopicLessonsFragment : InjectableFragment(), ExpandedChapterListIndexListener, StorySummarySelector {
  @Inject lateinit var topicLessonsFragmentPresenter: TopicLessonsFragmentPresenter

  private var currentExpandedChapterListIndex: Int? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (savedInstanceState != null) {
      currentExpandedChapterListIndex = savedInstanceState.getInt(KEY_CURRENT_EXPANDED_LIST_INDEX, -1)
      if (currentExpandedChapterListIndex == -1) {
        currentExpandedChapterListIndex = null
      }
    }
    return topicLessonsFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentExpandedChapterListIndex,
      this as ExpandedChapterListIndexListener
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedChapterListIndex != null) {
      outState.putInt(KEY_CURRENT_EXPANDED_LIST_INDEX, currentExpandedChapterListIndex!!)
    }
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedChapterListIndex = index
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    topicLessonsFragmentPresenter.storySummaryClicked(storySummary)
  }
}
