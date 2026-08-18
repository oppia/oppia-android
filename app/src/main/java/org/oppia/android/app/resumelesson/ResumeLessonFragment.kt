package org.oppia.android.app.resumelesson

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ResumeLessonFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that allows the user to resume a saved exploration. */
class ResumeLessonFragment : InjectableFragment() {
  companion object {

    /** Arguments key for [ResumeLessonFragment]. */
    const val RESUME_LESSON_FRAGMENT_ARGUMENTS_KEY = "ResumeLessonFragment.arguments"

    /** Creates new instance of [ResumeLessonFragment] for the provided parameters. */
    fun newInstance(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      parentScreen: ExplorationActivityParams.ParentScreen,
      checkpoint: ExplorationCheckpoint,
      readingTextSize: ReadingTextSize
    ): ResumeLessonFragment {
      val args = ResumeLessonFragmentArguments.newBuilder().apply {
        this.profileId = profileId
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
        this.parentScreen = parentScreen
        this.checkpoint = checkpoint
        this.readingTextSize = readingTextSize
      }.build()
      return ResumeLessonFragment().apply {
        arguments = Bundle().apply {
          putProto(RESUME_LESSON_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  @Inject
  lateinit var resumeLessonFragmentPresenter: ResumeLessonFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
    resumeLessonFragmentPresenter.handleAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be provided for fragment."
    }.getProto(
      RESUME_LESSON_FRAGMENT_ARGUMENTS_KEY,
      ResumeLessonFragmentArguments.getDefaultInstance()
    )
    return resumeLessonFragmentPresenter.handleOnCreate(
      inflater,
      container,
      args.profileId,
      args.classroomId,
      args.topicId,
      args.storyId,
      args.explorationId,
      args.parentScreen,
      args.checkpoint
    )
  }
}
