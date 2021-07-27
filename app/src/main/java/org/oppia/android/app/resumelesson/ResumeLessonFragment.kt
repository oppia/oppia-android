package org.oppia.android.app.resumelesson

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

class ResumeLessonFragment :
  InjectableFragment() {

  companion object {

    /** Creates new instance of [ResumeLessonFragment].
     * @param internalProfileId is used by the ResumeLessonFragment to retrieve saved checkpoint
     * @param explorationId is used by the ResumeLessonFragment to retrieve saved checkpoint
     */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      backflowScreen: Int
    ): ResumeLessonFragment {
      val resumeLessonFragment = ResumeLessonFragment()
      val args = Bundle()
      args.putInt(RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      args.putString(RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY, topicId)
      args.putString(RESUME_LESSON_FRAGMENT_STORY_ID_KEY, storyId)
      args.putString(RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY, explorationId)
      args.putInt(RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY, backflowScreen)
      resumeLessonFragment.arguments = args
      return resumeLessonFragment
    }
  }

  @Inject
  lateinit var resumeLessonFragmentPresenter: ResumeLessonFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId =
      arguments?.getInt(RESUME_LESSON_FRAGMENT_INTERNAL_PROFILE_ID_KEY, -1)!!
    val topicId =
      checkNotNull(arguments?.getString(RESUME_LESSON_FRAGMENT_TOPIC_ID_KEY)) {
        "Expected topic ID to be included in arguments for ResumeLessonFragment."
      }
    val storyId =
      checkNotNull(arguments?.getString(RESUME_LESSON_FRAGMENT_STORY_ID_KEY)) {
        "Expected story ID to be included in arguments for ResumeLessonFragment."
      }
    val explorationId =
      checkNotNull(arguments?.getString(RESUME_LESSON_FRAGMENT_EXPLORATION_ID_KEY)) {
        "Expected exploration ID to be included in arguments for ResumeLessonFragment."
      }
    val backflowScreen = arguments?.getInt(RESUME_LESSON_FRAGMENT_BACKFLOW_SCREEN_KEY, -1)
    return resumeLessonFragmentPresenter.handleOnCreate(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen
    )
  }
}
