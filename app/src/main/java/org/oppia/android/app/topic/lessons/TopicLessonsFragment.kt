package org.oppia.android.app.topic.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.topic.PROFILE_ID_EXTRA_KEY
import org.oppia.android.app.topic.STORY_ID_EXTRA_KEY
import org.oppia.android.app.topic.TOPIC_ID_EXTRA_KEY
import javax.inject.Inject

private const val CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY =
  "TopicLessonsFragment.current_expanded_list_index"

/** Fragment that contains subtopic list for lessons mode. */
class TopicLessonsFragment :
  InjectableFragment(),
  ExpandedChapterListIndexListener,
  StorySummarySelector {

  companion object {
    /** Returns a new [TopicLessonsFragment]. */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): TopicLessonsFragment {
      val topicLessonsFragment = TopicLessonsFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_EXTRA_KEY, internalProfileId)
      args.putString(TOPIC_ID_EXTRA_KEY, topicId)

      if (storyId.isNotEmpty())
        args.putString(STORY_ID_EXTRA_KEY, storyId)

      topicLessonsFragment.arguments = args
      return topicLessonsFragment
    }
  }

  @Inject
  lateinit var topicLessonsFragmentPresenter: TopicLessonsFragmentPresenter

  private var currentExpandedChapterListIndex: Int? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (savedInstanceState != null) {
      currentExpandedChapterListIndex =
        savedInstanceState.getInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, -1)
      if (currentExpandedChapterListIndex == -1) {
        currentExpandedChapterListIndex = null
      }
    }
    val internalProfileId = arguments?.getInt(PROFILE_ID_EXTRA_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getString(TOPIC_ID_EXTRA_KEY)) {
      "Expected topic ID to be included in arguments for TopicLessonsFragment."
    }
    val storyId = arguments?.getString(STORY_ID_EXTRA_KEY) ?: ""

    return topicLessonsFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentExpandedChapterListIndex,
      this as ExpandedChapterListIndexListener,
      internalProfileId,
      topicId,
      storyId
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedChapterListIndex != null) {
      outState.putInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, currentExpandedChapterListIndex!!)
    }
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedChapterListIndex = index
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    topicLessonsFragmentPresenter.storySummaryClicked(storySummary)
  }
}
