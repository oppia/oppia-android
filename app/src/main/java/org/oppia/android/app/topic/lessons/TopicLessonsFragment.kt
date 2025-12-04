package org.oppia.android.app.topic.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.TopicLessonsFragmentArguments
import org.oppia.android.app.model.TopicLessonsFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
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
    /** Arguments key for TopicLessonsFragment. */
    const val TOPIC_LESSONS_FRAGMENT_ARGUMENTS_KEY = "TopicLessonsFragment.arguments"

    /** State key for TopicLessonsFragment. */
    const val TOPIC_LESSONS_FRAGMENT_STATE_KEY = "TopicLessonsFragment.state"

    /** Returns a new [TopicLessonsFragment]. */
    fun newInstance(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String
    ): TopicLessonsFragment {

      val args = TopicLessonsFragmentArguments.newBuilder().apply {
        this.classroomId = classroomId
        this.topicId = topicId
        if (storyId.isNotBlank())
          this.storyId = storyId
      }.build()
      return TopicLessonsFragment().apply {
        arguments = Bundle().apply {
          putProto(TOPIC_LESSONS_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
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
      val stateArgs = savedInstanceState.getProto(
        TOPIC_LESSONS_FRAGMENT_STATE_KEY,
        TopicLessonsFragmentStateBundle.getDefaultInstance()
      )
      currentExpandedChapterListIndex =
        stateArgs?.currentExpandedChapterListIndex ?: -1
      if (currentExpandedChapterListIndex == -1) {
        currentExpandedChapterListIndex = null
      }
      isDefaultStoryExpanded = stateArgs?.isDefaultStoryExpanded ?: false
    }
    val profileId = arguments?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    val args = arguments?.getProto(
      TOPIC_LESSONS_FRAGMENT_ARGUMENTS_KEY,
      TopicLessonsFragmentArguments.getDefaultInstance()
    )
    val classroomId = checkNotNull(args?.classroomId) {
      "Expected classroom ID to be included in arguments for TopicLessonsFragment."
    }
    val topicId = checkNotNull(args?.topicId) {
      "Expected topic ID to be included in arguments for TopicLessonsFragment."
    }
    val storyId = args?.storyId ?: ""
    return topicLessonsFragmentPresenter.handleCreateView(
      inflater,
      container,
      currentExpandedChapterListIndex,
      this as ExpandedChapterListIndexListener,
      profileId,
      classroomId,
      topicId,
      storyId,
      isDefaultStoryExpanded
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    val args = TopicLessonsFragmentStateBundle.newBuilder().apply {
      if (this@TopicLessonsFragment.currentExpandedChapterListIndex != null) {
        this.currentExpandedChapterListIndex =
          this@TopicLessonsFragment.currentExpandedChapterListIndex!!
      }
      this.isDefaultStoryExpanded = this@TopicLessonsFragment.isDefaultStoryExpanded
    }.build()
    outState.putProto(
      TOPIC_LESSONS_FRAGMENT_STATE_KEY,
      args
    )
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
