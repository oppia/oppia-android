package org.oppia.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.utility.FontScaleConfigurationUtil
import javax.inject.Inject

/** Fragment that contains displays single exploration. */
class ExplorationFragment : InjectableFragment() {
  @Inject
  lateinit var explorationFragmentPresenter: ExplorationFragmentPresenter

  @Inject
  lateinit var fontScaleConfigurationUtil: FontScaleConfigurationUtil

  companion object {
    internal const val INTERNAL_PROFILE_ID_ARGUMENT_KEY =
      "ExplorationFragment.internal_profile_id"
    internal const val TOPIC_ID_ARGUMENT_KEY = "ExplorationFragment.topic_id"
    internal const val STORY_ID_ARGUMENT_KEY = "ExplorationFragment.story_id"
    internal const val STORY_DEFAULT_FONT_SIZE_ARGUMENT_KEY =
      "ExplorationFragment.story_default_font_size"
    internal const val EXPLORATION_ID_ARGUMENT_KEY =
      "ExplorationFragment.exploration_id"

    /** Returns a new [ExplorationFragment] to pass the profileId, topicId, storyId, storyTextSize and explorationId. */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String,
      storyTextSize: String,
      explorationId: String
    ): ExplorationFragment {
      val explorationFragment = ExplorationFragment()
      val args = Bundle()
      args.putInt(
        INTERNAL_PROFILE_ID_ARGUMENT_KEY,
        internalProfileId
      )
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      args.putString(STORY_ID_ARGUMENT_KEY, storyId)
      args.putString(
        STORY_DEFAULT_FONT_SIZE_ARGUMENT_KEY,
        storyTextSize
      )
      args.putString(
        EXPLORATION_ID_ARGUMENT_KEY,
        explorationId
      )
      explorationFragment.arguments = args
      return explorationFragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
    val storyTextSize =
      arguments!!.getString(STORY_DEFAULT_FONT_SIZE_ARGUMENT_KEY)
    checkNotNull(storyTextSize) { "ExplorationFragment must be created with a story text size" }
    fontScaleConfigurationUtil.adjustFontScale(context, storyTextSize)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val profileId =
      arguments!!.getInt(INTERNAL_PROFILE_ID_ARGUMENT_KEY, -1)
    val topicId =
      arguments!!.getString(TOPIC_ID_ARGUMENT_KEY)
    checkNotNull(topicId) { "StateFragment must be created with an topic ID" }
    val storyId =
      arguments!!.getString(STORY_ID_ARGUMENT_KEY)
    checkNotNull(storyId) { "StateFragment must be created with an story ID" }
    val explorationId =
      arguments!!.getString(EXPLORATION_ID_ARGUMENT_KEY)
    checkNotNull(explorationId) { "StateFragment must be created with an exploration ID" }
    return explorationFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId,
      storyId,
      explorationId
    )
  }

  fun handlePlayAudio() = explorationFragmentPresenter.handlePlayAudio()

  fun onKeyboardAction() = explorationFragmentPresenter.onKeyboardAction()

  fun setAudioBarVisibility(isVisible: Boolean) =
    explorationFragmentPresenter.setAudioBarVisibility(isVisible)

  fun scrollToTop() = explorationFragmentPresenter.scrollToTop()

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    explorationFragmentPresenter.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean) {
    explorationFragmentPresenter.revealSolution(saveUserChoice)
  }
}
