package org.oppia.android.app.resumelesson

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.util.extensions.getStringFromBundle

/** Fragment that allows the user to resume a saved exploration. */
class ResumeLessonFragment : InjectableFragment() {

  companion object {
    private const val RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY =
      "ResumeExplorationFragmentPresenter.resume_exploration_fragment_internal_profile_id"
    private const val RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY =
      "ResumeExplorationFragmentPresenter.resume_exploration_fragment_topic_id"
    private const val RESUME_LESSON_FRAGMENT_STORY_ID_KEY =
      "ResumeExplorationFragmentPresenter.resume_exploration_fragment_story_id"
    private const val RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY =
      "ResumeExplorationFragmentPresenter.resume_Lesson_fragment_exploration_id"
    private const val RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY =
      "ResumeLessonFragmentPresenter.resume_lesson_fragment_backflow_screen"
    private const val RESUME_LESSON_FRAGMENT_EXPLORATION_CHECKPOINT_KEY =
      "ResumeExplorationFragmentPresenter.resume_Lesson_fragment_exploration_checkpoint"

    /**
     * Creates new instance of [ResumeLessonFragment].
     *
     * @param internalProfileId is used by the ResumeLessonFragment to retrieve saved checkpoint
     * @param explorationId is used by the ResumeLessonFragment to retrieve saved checkpoint
     */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      backflowScreen: Int,
      explorationCheckpoint: ExplorationCheckpoint
    ): ResumeLessonFragment {
      val resumeLessonFragment = ResumeLessonFragment()
      val args = Bundle()
      args.putInt(RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      args.putString(RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY, topicId)
      args.putString(RESUME_LESSON_FRAGMENT_STORY_ID_KEY, storyId)
      args.putString(RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY, explorationId)
      args.putInt(RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY, backflowScreen)
      args.putProto(RESUME_LESSON_FRAGMENT_EXPLORATION_CHECKPOINT_KEY, explorationCheckpoint)
      resumeLessonFragment.arguments = args
      return resumeLessonFragment
    }
  }

  @Inject
  lateinit var resumeLessonFragmentPresenter: ResumeLessonFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId =
      checkNotNull(arguments?.getInt(RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY, -1)) {
        "Expected profile ID to be included in arguments for ResumeLessonFragment."
      }
    val topicId =
      checkNotNull(arguments?.getStringFromBundle(RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY)) {
        "Expected topic ID to be included in arguments for ResumeLessonFragment."
      }
    val storyId =
      checkNotNull(arguments?.getStringFromBundle(RESUME_LESSON_FRAGMENT_STORY_ID_KEY)) {
        "Expected story ID to be included in arguments for ResumeLessonFragment."
      }
    val explorationId =
      checkNotNull(arguments?.getStringFromBundle(RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY)) {
        "Expected exploration ID to be included in arguments for ResumeLessonFragment."
      }
    val backflowScreen = arguments?.getInt(RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY, -1)
    val explorationCheckpoint =
      checkNotNull(
        arguments?.getProto(
          RESUME_LESSON_FRAGMENT_EXPLORATION_CHECKPOINT_KEY,
          ExplorationCheckpoint.getDefaultInstance()
        )
      ) {
        "Expected exploration checkpoint to be included in arguments for ResumeLessonFragment."
      }

    return resumeLessonFragmentPresenter.handleOnCreate(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
      explorationCheckpoint
    )
  }
}
