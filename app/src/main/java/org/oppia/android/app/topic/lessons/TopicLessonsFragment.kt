package org.oppia.android.app.topic.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.topic.STORY_ID_ARGUMENT_KEY
import org.oppia.android.app.topic.TOPIC_ID_ARGUMENT_KEY
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

private const val CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY =
  "TopicLessonsFragment.current_expanded_list_index"
private const val IS_DEFAULT_STORY_SHOWN = "TopicLessonsFragment.is_default_story_shown"

/** Fragment that contains subtopic list for lessons mode. */
class TopicLessonsFragment :
  InjectableFragment(),
  ExpandedChapterListIndexListener,
  StorySummarySelector,
  ChapterSummarySelector {

  companion object {
    /** Returns a new [TopicLessonsFragment]. */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): TopicLessonsFragment {
      val topicLessonsFragment = TopicLessonsFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)

      if (storyId.isNotEmpty())
        args.putString(STORY_ID_ARGUMENT_KEY, storyId)

      topicLessonsFragment.arguments = args
      return topicLessonsFragment
    }
  }

  @Inject
  lateinit var topicLessonsFragmentPresenter: TopicLessonsFragmentPresenter

  private var currentExpandedChapterListIndex: Int? = null
  private var isDefaultStoryExpanded: Boolean = false

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
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
      isDefaultStoryExpanded = savedInstanceState.getBoolean(IS_DEFAULT_STORY_SHOWN)
    }
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicLessonsFragment."
    }
    val storyId = arguments?.getStringFromBundle(STORY_ID_ARGUMENT_KEY) ?: ""

    return topicLessonsFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentExpandedChapterListIndex,
      this as ExpandedChapterListIndexListener,
      internalProfileId,
      topicId,
      storyId,
      isDefaultStoryExpanded
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedChapterListIndex != null) {
      outState.putInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, currentExpandedChapterListIndex!!)
    }
    outState.putBoolean(IS_DEFAULT_STORY_SHOWN, isDefaultStoryExpanded)
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedChapterListIndex = index
    isDefaultStoryExpanded = true
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    topicLessonsFragmentPresenter.storySummaryClicked(storySummary)
  }

  override fun selectChapterSummary(
    storyId: String,
    explorationId: String,
    chapterPlayState: ChapterPlayState
  ) {
    topicLessonsFragmentPresenter.selectChapterSummary(storyId, explorationId, chapterPlayState)
  }
}
