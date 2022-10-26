package org.oppia.android.app.player.exploration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ExplorationFragmentArguments
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.databinding.ExplorationFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject
import kotlinx.android.synthetic.main.toolbar.*
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget

/** The presenter for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val profileManagementController: ProfileManagementController,
  private val spotlightFragment: SpotlightFragment
) {
  /** Handles the [Fragment.onAttach] portion of [ExplorationFragment]'s lifecycle. */
  fun handleAttach(context: Context) {
    fontScaleConfigurationUtil.adjustFontScale(context, retrieveArguments().readingTextSize)
  }

  /** Handles the [Fragment.onCreateView] portion of [ExplorationFragment]'s lifecycle. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    val args = retrieveArguments()
    val binding =
      ExplorationFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root
    val stateFragment =
      StateFragment.newInstance(
        args.profileId.internalId, args.topicId, args.storyId, args.explorationId
      )
    logPracticeFragmentEvent(args.topicId, args.storyId, args.explorationId)
    if (getStateFragment() == null) {
      fragment.childFragmentManager.beginTransaction().add(
        R.id.state_fragment_placeholder,
        stateFragment
      ).commitNow()
    }
    return binding
  }

  /** Handles the [Fragment.onViewCreated] portion of [ExplorationFragment]'s lifecycle. */
  fun handleViewCreated(view: View) {
    val profileDataProvider = profileManagementController.getProfile(retrieveArguments().profileId)
    profileDataProvider.toLiveData().observe(
      fragment,
      { result ->
        val readingTextSize = retrieveArguments().readingTextSize
        if (result is AsyncResult.Success && result.value.readingTextSize != readingTextSize) {
          selectNewReadingTextSize(result.value.readingTextSize)

          // Since text views are based on sp for sizing, the activity needs to be recreated so that
          // sp can be correctly recomputed.
          fragment.requireActivity().recreate()
        }
      }
    )
    val toolbar: Toolbar = view.findViewById(R.id.exploration_toolbar)
      toolbar.forEach {
      if (it.id == R.id.action_audio_player) {
        it.post {
          val targetList = arrayListOf(
            SpotlightTarget (
              it,
              "Would you like Oppia to read for you? Tap on this button to try!",
              SpotlightShape.Circle,
              Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
              )
          )

          spotlightFragment.initialiseTargetList(targetList, 123)
          fragment.childFragmentManager.beginTransaction()
            .add(spotlightFragment, "")
            .commitNow()
        }
      }
    }
  }

  fun handlePlayAudio() {
    getStateFragment()?.handlePlayAudio()
  }

  fun setAudioBarVisibility(isVisible: Boolean) =
    getStateFragment()?.setAudioBarVisibility(isVisible)

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun onKeyboardAction() {
    getStateFragment()?.handleKeyboardAction()
  }

  fun revealHint(hintIndex: Int) {
    getStateFragment()?.revealHint(hintIndex)
  }

  fun revealSolution() {
    getStateFragment()?.revealSolution()
  }

  fun dismissConceptCard() = getStateFragment()?.dismissConceptCard()

  fun getExplorationCheckpointState() = getStateFragment()?.getExplorationCheckpointState()

  private fun getStateFragment(): StateFragment? {
    return fragment
      .childFragmentManager
      .findFragmentById(
        R.id.state_fragment_placeholder
      ) as StateFragment?
  }

  private fun logPracticeFragmentEvent(topicId: String, storyId: String, explorationId: String) {
    oppiaLogger.logImportantEvent(
      oppiaLogger.createOpenExplorationActivityContext(topicId, storyId, explorationId)
    )
  }

  private fun selectNewReadingTextSize(readingTextSize: ReadingTextSize) {
    updateArguments(
      retrieveArguments().toBuilder().apply {
        this.readingTextSize = readingTextSize
      }.build()
    )
    fontScaleConfigurationUtil.adjustFontScale(fragment.requireActivity(), readingTextSize)
  }

  private fun retrieveArguments(): ExplorationFragmentArguments {
    return fragment.requireArguments().getProto(
      ARGUMENTS_KEY, ExplorationFragmentArguments.getDefaultInstance()
    )
  }

  private fun updateArguments(updatedArgs: ExplorationFragmentArguments) {
    fragment.requireArguments().putProto(ARGUMENTS_KEY, updatedArgs)
  }

  companion object {
    /** The fragment arguments key for all proto-held arguments for [ExplorationFragment]. */
    const val ARGUMENTS_KEY = "ExplorationFragment.arguments"
  }
}
