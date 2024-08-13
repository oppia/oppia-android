package org.oppia.android.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ExplorationFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that contains displays single exploration. */
class ExplorationFragment : InjectableFragment() {
  @Inject
  lateinit var explorationFragmentPresenter: ExplorationFragmentPresenter

  companion object {
    /** Returns a new [ExplorationFragment] with the corresponding fragment parameters. */
    fun newInstance(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      readingTextSize: ReadingTextSize
    ): ExplorationFragment {
      val args = ExplorationFragmentArguments.newBuilder().apply {
        this.profileId = profileId
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
        this.readingTextSize = readingTextSize
      }.build()
      return ExplorationFragment().apply {
        arguments = Bundle().apply {
          putProto(ExplorationFragmentPresenter.ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
    explorationFragmentPresenter.handleAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = explorationFragmentPresenter.handleCreateView(inflater, container)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    explorationFragmentPresenter.handleViewCreated()
  }

  fun handlePlayAudio() = explorationFragmentPresenter.handlePlayAudio()

  fun onKeyboardAction() = explorationFragmentPresenter.onKeyboardAction()

  fun setAudioBarVisibility(isVisible: Boolean) =
    explorationFragmentPresenter.setAudioBarVisibility(isVisible)

  fun scrollToTop() = explorationFragmentPresenter.scrollToTop()

  fun revealHint(hintIndex: Int) {
    explorationFragmentPresenter.revealHint(hintIndex)
  }

  fun viewHint(hintIndex: Int) {
    explorationFragmentPresenter.viewHint(hintIndex)
  }
  fun revealSolution() {
    explorationFragmentPresenter.revealSolution()
  }
  fun viewSolution() {
    explorationFragmentPresenter.viewSolution()
  }

  fun dismissConceptCard() = explorationFragmentPresenter.dismissConceptCard()

  fun getExplorationCheckpointState() = explorationFragmentPresenter.getExplorationCheckpointState()
}
