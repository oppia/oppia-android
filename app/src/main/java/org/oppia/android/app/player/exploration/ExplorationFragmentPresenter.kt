package org.oppia.android.app.player.exploration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import javax.inject.Inject
import kotlinx.android.synthetic.main.exploration_activity.*
import kotlinx.android.synthetic.main.exploration_activity.view.*
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ExplorationFragmentArguments
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.topic.SPOTLIGHT_FRAGMENT_TAG
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.databinding.ExplorationFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** The presenter for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val profileManagementController: ProfileManagementController,
  private val topicListController: TopicListController
) {

  private var internalProfileId: Int = -1

  /** Handles the [Fragment.onAttach] portion of [ExplorationFragment]'s lifecycle. */
  fun handleAttach(context: Context) {
    fontScaleConfigurationUtil.adjustFontScale(context, retrieveArguments().readingTextSize)
  }

  /** Handles the [Fragment.onCreateView] portion of [ExplorationFragment]'s lifecycle. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    val args = retrieveArguments()
    val binding =
      ExplorationFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root
    internalProfileId = args.profileId.internalId
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
  fun handleViewCreated() {
    val profileDataProvider = profileManagementController.getProfile(retrieveArguments().profileId)
    profileDataProvider.toLiveData().observe(
      fragment
    ) { result ->
      val readingTextSize = retrieveArguments().readingTextSize
      if (result is AsyncResult.Success && result.value.readingTextSize != readingTextSize) {
        selectNewReadingTextSize(result.value.readingTextSize)

        // Since text views are based on sp for sizing, the activity needs to be recreated so that
        // sp can be correctly recomputed.
        fragment.requireActivity().recreate()
      } else {
        if (result is AsyncResult.Success) {
          showSpotlights(result.value.numberOfLogins)
        }
      }
    }
  }

  private fun showSpotlights(numberOfLogins: Int) {
    val explorationToolbar =
      (fragment.requireActivity() as AppCompatActivity).exploration_toolbar
    explorationToolbar.forEach {
      if (it is ImageButton) {
        // this toolbar contains only one image button, which is the back navigation icon
        val backButtonSpotlightTarget = SpotlightTarget(
          it,
          fragment.requireContext().getString(R.string.exploration_exit_button_spotlight_hint),
          SpotlightShape.Circle,
          Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
        )
        checkNotNull(getSpotlightFragment()).requestSpotlight(backButtonSpotlightTarget)

        // spotlight voice-over icon after 3 logins
        if (numberOfLogins >= 3) {
          val audioPlayerSpotlightTarget = SpotlightTarget(
            explorationToolbar.action_audio_player,
            fragment.requireContext().getString(R.string.voiceover_icon_spotlight_hint),
            SpotlightShape.Circle,
            Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
          )
          checkNotNull(getSpotlightFragment()).requestSpotlight(audioPlayerSpotlightTarget)
        }
      }
    }
  }

  private fun getSpotlightFragment(): SpotlightFragment? {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SPOTLIGHT_FRAGMENT_TAG
    ) as SpotlightFragment?
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
