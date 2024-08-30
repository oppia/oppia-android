package org.oppia.android.app.player.exploration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ExplorationFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.databinding.ExplorationFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The presenter for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val profileManagementController: ProfileManagementController,
  private val resourceHandler: AppLanguageResourceHandler
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
    logPracticeFragmentEvent(args.classroomId, args.topicId, args.storyId, args.explorationId)
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
      if (result is AsyncResult.Success) {
        if (result.value.readingTextSize != readingTextSize) {

          // Since text views are based on sp for sizing, the activity needs to be recreated so that
          // sp can be correctly recomputed.
          selectNewReadingTextSize(result.value.readingTextSize)
          fragment.requireActivity().recreate()
        } else showSpotlights(result.value.numberOfLogins)
      }
    }
  }

  private fun showSpotlights(numberOfLogins: Int) {
    val explorationToolbar =
      fragment.requireActivity().findViewById<View>(R.id.exploration_toolbar) as Toolbar
    explorationToolbar.forEach {
      if (it is ImageButton) {
        // This toolbar contains only one image button, which is the back navigation icon.
        val backButtonSpotlightTarget = SpotlightTarget(
          it,
          resourceHandler.getStringInLocale(R.string.exploration_exit_button_spotlight_hint),
          SpotlightShape.Circle,
          Spotlight.FeatureCase.LESSONS_BACK_BUTTON
        )
        checkNotNull(getSpotlightManager()).requestSpotlight(backButtonSpotlightTarget)
      }
    }

    (fragment.requireActivity() as RequestVoiceOverIconSpotlightListener)
      .requestVoiceOverIconSpotlight(numberOfLogins)
  }

  private fun getSpotlightManager(): SpotlightManager? {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager
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
  fun viewHint(hintIndex: Int) {
    getStateFragment()?.viewHint(hintIndex)
  }

  fun revealSolution() {
    getStateFragment()?.revealSolution()
  }

  fun viewSolution() {
    getStateFragment()?.viewSolution()
  }

  fun getExplorationCheckpointState() = getStateFragment()?.getExplorationCheckpointState()

  private fun getStateFragment(): StateFragment? {
    return fragment
      .childFragmentManager
      .findFragmentById(
        R.id.state_fragment_placeholder
      ) as StateFragment?
  }

  private fun logPracticeFragmentEvent(
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ) {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenExplorationActivityContext(
        classroomId, topicId, storyId, explorationId
      ),
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build()
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
