package org.oppia.android.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralExploration
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.stopplaying.ProgressDatabaseFullDialogFragment
import org.oppia.android.app.player.stopplaying.UnsavedExplorationDialogFragment
import org.oppia.android.app.spotlight.SpotlightFragment
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.topic.PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ExplorationActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val TAG_UNSAVED_EXPLORATION_DIALOG = "UNSAVED_EXPLORATION_DIALOG"
private const val TAG_STOP_EXPLORATION_DIALOG = "STOP_EXPLORATION_DIALOG"
private const val TAG_PROGRESS_DATABASE_FULL_DIALOG = "PROGRESS_DATABASE_FULL_DIALOG"
private const val TAG_EXPLORATION_FRAGMENT = "TAG_EXPLORATION_FRAGMENT"
private const val TAG_EXPLORATION_MANAGER_FRAGMENT = "TAG_EXPLORATION_MANAGER_FRAGMENT"
const val TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER = "HINTS_AND_SOLUTION_EXPLORATION_MANAGER"

/** The Presenter for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val viewModelProvider: ViewModelProvider<ExplorationViewModel>,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var explorationToolbar: Toolbar
  private lateinit var explorationToolbarTitle: TextView
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var context: Context
  private lateinit var parentScreen: ExplorationActivityParams.ParentScreen

  private var isCheckpointingEnabled: Boolean = false

  private lateinit var oldestCheckpointExplorationId: String
  private lateinit var oldestCheckpointExplorationTitle: String
  private lateinit var binding: ExplorationActivityBinding

  private val exploreViewModel by lazy {
    getExplorationViewModel()
  }

  fun handleOnCreate(
    context: Context,
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    binding = DataBindingUtil.setContentView<ExplorationActivityBinding>(
      activity,
      R.layout.exploration_activity
    )
    binding.apply {
      viewModel = exploreViewModel
      lifecycleOwner = activity
    }

    explorationToolbar = binding.explorationToolbar
    explorationToolbarTitle = binding.explorationToolbarTitle
    activity.setSupportActionBar(explorationToolbar)

    binding.explorationToolbarTitle.setOnClickListener {
      binding.explorationToolbarTitle.isSelected = true
    }

    binding.explorationToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }

    binding.actionAudioPlayer.setOnClickListener {
      getExplorationFragment()?.handlePlayAudio()
    }

    binding.actionBottomSheetOptionsMenu.setOnClickListener {
      val bottomSheetOptionsMenu = BottomSheetOptionsMenu()
      bottomSheetOptionsMenu.showNow(activity.supportFragmentManager, bottomSheetOptionsMenu.tag)
    }

    this.profileId = profileId
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.context = context
    this.parentScreen = parentScreen
    this.isCheckpointingEnabled = isCheckpointingEnabled
    updateToolbarTitle(explorationId)

    // Retrieve oldest saved checkpoint details.
    subscribeToOldestSavedExplorationDetails()

    if (getExplorationManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        ExplorationManagerFragment.createNewInstance(profileId),
        TAG_EXPLORATION_MANAGER_FRAGMENT
      ).commitNow()
    }

    if (getSpotlightFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_spotlight_fragment_placeholder,
        SpotlightFragment.newInstance(profileId.internalId),
        SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
      ).commitNow()
    }
  }

  fun requestVoiceOverIconSpotlight(numberOfLogins: Int) {
    if (numberOfLogins >= 3) {
      // Spotlight the voice-over icon after 3 or more logins.
      val audioPlayerSpotlightTarget = SpotlightTarget(
        binding.actionAudioPlayer,
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.voiceover_icon_spotlight_hint,
          resourceHandler.getStringInLocale(R.string.app_name)
        ),
        SpotlightShape.Circle,
        Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
      )
      checkNotNull(getSpotlightFragment()).requestSpotlightViewWithDelayedLayout(
        audioPlayerSpotlightTarget
      )
    }
  }

  private fun getSpotlightFragment(): SpotlightFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightFragment
  }

  fun loadExplorationFragment(readingTextSize: ReadingTextSize) {
    if (getExplorationFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        ExplorationFragment.newInstance(
          profileId, topicId, storyId, explorationId, readingTextSize
        ),
        TAG_EXPLORATION_FRAGMENT
      ).commitNow()
    }

    if (getHintsAndSolutionManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        HintsAndSolutionExplorationManagerFragment(),
        TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
      ).commitNow()
    }
  }

  /** Action for onOptionsItemSelected */
  fun handleOnOptionsItemSelected(itemId: Int): Boolean {
    return when (itemId) {
      R.id.action_options -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity,
          profileId.internalId,
          /* isFromNavigationDrawer= */ false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
        context.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity, profileId.internalId,
          /* isFromNavigationDrawer= */false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
        context.startActivity(intent)
        true
      }
      else -> false
    }
  }

  fun showAudioButton() = exploreViewModel.showAudioButton.set(true)

  fun hideAudioButton() = exploreViewModel.showAudioButton.set(false)

  fun showAudioStreamingOn() = exploreViewModel.isAudioStreamingOn.set(true)

  fun showAudioStreamingOff() = exploreViewModel.isAudioStreamingOn.set(false)

  fun setAudioBarVisibility(isVisible: Boolean) =
    getExplorationFragment()?.setAudioBarVisibility(isVisible)

  fun scrollToTop() = getExplorationFragment()?.scrollToTop()

  private fun getExplorationManagerFragment(): ExplorationManagerFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_EXPLORATION_MANAGER_FRAGMENT
    ) as? ExplorationManagerFragment
  }

  private fun getExplorationFragment(): ExplorationFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_EXPLORATION_FRAGMENT
    ) as? ExplorationFragment
  }

  private fun getHintsAndSolutionManagerFragment(): HintsAndSolutionExplorationManagerFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
    ) as HintsAndSolutionExplorationManagerFragment?
  }

  /** Deletes the saved progress for the current exploration and then stops the exploration. */
  fun deleteCurrentProgressAndStopExploration(isCompletion: Boolean) {
    explorationDataController.deleteExplorationProgressById(profileId, explorationId)
    stopExploration(isCompletion)
  }

  /** Deletes the oldest saved checkpoint and then stops the exploration. */
  fun deleteOldestSavedProgressAndStopExploration() {
    // If oldestCheckpointExplorationId is not initialized, it means that there was an error while
    // retrieving the oldest saved checkpoint details. In this case, the exploration is exited
    // without deleting any checkpoints.
    if (::oldestCheckpointExplorationId.isInitialized) {
      explorationDataController.deleteExplorationProgressById(
        profileId, oldestCheckpointExplorationId
      )
    }
    stopExploration(isCompletion = false)
  }

  fun stopExploration(isCompletion: Boolean) {
    fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
    explorationDataController.stopPlayingExploration(isCompletion).toLiveData()
      .observe(
        activity,
        Observer<AsyncResult<Any?>> {
          when (it) {
            is AsyncResult.Pending -> oppiaLogger.d("ExplorationActivity", "Stopping exploration")
            is AsyncResult.Failure ->
              oppiaLogger.e("ExplorationActivity", "Failed to stop exploration", it.error)
            is AsyncResult.Success -> {
              oppiaLogger.d("ExplorationActivity", "Successfully stopped exploration")
              backPressActivitySelector()
              (activity as ExplorationActivity).finish()
            }
          }
        }
      )
  }

  fun onKeyboardAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      val explorationFragment = activity.supportFragmentManager.findFragmentByTag(
        TAG_EXPLORATION_FRAGMENT
      ) as? ExplorationFragment
      explorationFragment?.onKeyboardAction()
    }
  }

  /**
   * Shows an appropriate dialog box or exits the exploration directly without showing any dialog
   * box when back button is pressed. This function shows [UnsavedExplorationDialogFragment] if
   * checkpointing is not enabled otherwise it either exits the exploration or shows
   * [ProgressDatabaseFullDialogFragment] depending upon the state of the saved checkpoint for the
   * current exploration.
   */
  fun backButtonPressed() {
    // If checkpointing is not enabled, show StopExplorationDialogFragment to exit the exploration,
    // this is expected to happen if the exploration is marked as completed.
    if (!isCheckpointingEnabled) {
      showUnsavedExplorationDialogFragment()
      return
    }
    // If checkpointing is enabled, get the current checkpoint state to show an appropriate dialog
    // fragment.
    showDialogFragmentBasedOnCurrentCheckpointState()
  }

  fun dismissConceptCard() {
    getExplorationFragment()?.dismissConceptCard()
  }

  private fun updateToolbarTitle(explorationId: String) {
    subscribeToExploration(
      explorationDataController.getExplorationById(profileId, explorationId).toLiveData()
    )
  }

  private fun subscribeToExploration(
    explorationResultLiveData: LiveData<AsyncResult<EphemeralExploration>>
  ) {
    val explorationLiveData = getEphemeralExploration(explorationResultLiveData)
    explorationLiveData.observe(activity) {
      explorationToolbarTitle.text =
        translationController.extractString(
          it.exploration.translatableTitle, it.writtenTranslationContext
        )
    }
  }

  private fun getExplorationViewModel(): ExplorationViewModel {
    return viewModelProvider.getForActivity(activity, ExplorationViewModel::class.java)
  }

  /** Helper for subscribeToExploration. */
  private fun getEphemeralExploration(
    exploration: LiveData<AsyncResult<EphemeralExploration>>
  ): LiveData<EphemeralExploration> {
    return Transformations.map(exploration, ::processEphemeralExploration)
  }

  /** Helper for subscribeToExploration. */
  private fun processEphemeralExploration(
    ephemeralExpResult: AsyncResult<EphemeralExploration>
  ): EphemeralExploration {
    return when (ephemeralExpResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ExplorationActivity", "Failed to retrieve answer outcome", ephemeralExpResult.error
        )
        EphemeralExploration.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralExploration.getDefaultInstance()
      is AsyncResult.Success -> ephemeralExpResult.value
    }
  }

  private fun backPressActivitySelector() {
    when (parentScreen) {
      ExplorationActivityParams.ParentScreen.TOPIC_SCREEN_LESSONS_TAB,
      ExplorationActivityParams.ParentScreen.STORY_SCREEN -> activity.finish()
      ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
      ExplorationActivityParams.ParentScreen.UNRECOGNIZED -> {
        // Default to the topic activity.
        activity.startActivity(
          TopicActivity.createTopicActivityIntent(context, profileId.internalId, topicId)
        )
      }
    }
  }

  fun revealHint(hintIndex: Int) {
    val explorationFragment =
      activity.supportFragmentManager.findFragmentByTag(
        TAG_EXPLORATION_FRAGMENT
      ) as ExplorationFragment
    explorationFragment.revealHint(hintIndex)
  }

  fun revealSolution() {
    val explorationFragment =
      activity.supportFragmentManager.findFragmentByTag(
        TAG_EXPLORATION_FRAGMENT
      ) as ExplorationFragment
    explorationFragment.revealSolution()
  }

  private fun showProgressDatabaseFullDialogFragment() {
    val previousFragment = activity.supportFragmentManager.findFragmentByTag(
      TAG_PROGRESS_DATABASE_FULL_DIALOG
    )
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    // If any one of oldestCheckpointExplorationId or oldestCheckpointExplorationTitle is not
    // initialized, it means that there was an error while retrieving the oldest saved checkpoint
    // details or no checkpoint was found. In this case, exit without deleting any checkpoints.
    if (
      !::oldestCheckpointExplorationId.isInitialized ||
      !::oldestCheckpointExplorationTitle.isInitialized
    ) {
      stopExploration(isCompletion = false)
      return
    }

    val dialogFragment =
      ProgressDatabaseFullDialogFragment.newInstance(oldestCheckpointExplorationTitle)
    dialogFragment.showNow(
      activity.supportFragmentManager,
      TAG_PROGRESS_DATABASE_FULL_DIALOG
    )
  }

  private fun showUnsavedExplorationDialogFragment() {
    val previousFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_UNSAVED_EXPLORATION_DIALOG)
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = UnsavedExplorationDialogFragment.newInstance()
    dialogFragment.showNow(
      activity.supportFragmentManager,
      TAG_UNSAVED_EXPLORATION_DIALOG
    )
  }

  /**
   * Listens to the result of [ExplorationDataController.getOldestExplorationDetailsDataProvider].
   *
   * If the result is success it updates the value of the variables [oldestCheckpointExplorationId]
   * and [oldestCheckpointExplorationTitle]. If the result fails, it does not initializes the
   * variables [oldestCheckpointExplorationId] and [oldestCheckpointExplorationTitle] with any
   * value. In cases when no checkpoint was found, [oldestCheckpointExplorationId] will remain
   * uninitialized.
   *
   * Since this function is kicked off before any other save operation, it is expected to complete
   * before any following save operations complete.
   *
   * If the operations fails or this function does not get enough time to complete, the user is not
   * blocked. Instead, the flow of the application proceeds as if the checkpoints were not found. In
   * that case, the variables [oldestCheckpointExplorationId] and [oldestCheckpointExplorationTitle]
   * are not initialized.
   */
  private fun subscribeToOldestSavedExplorationDetails() {
    explorationDataController.getOldestExplorationDetailsDataProvider(
      profileId
    ).toLiveData().observe(activity) {
      when (it) {
        is AsyncResult.Success -> {
          // Only set the exploration parameters if a checkpoint was found.
          if (it.value.explorationId.isNotEmpty()) {
            oldestCheckpointExplorationId = it.value.explorationId
            oldestCheckpointExplorationTitle = it.value.explorationTitle
          }
        }
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "ExplorationActivity", "Failed to retrieve oldest saved checkpoint details.", it.error
          )
        }
        is AsyncResult.Pending -> {} // Wait for an actual result.
      }
    }
  }

  /**
   * Checks the checkpointState for the current exploration and shows an appropriate dialog
   * fragment.
   *
   * If the checkpointState is equal to CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT,
   * exploration will be stopped without showing any dialogFragment. If the checkpointState is equal
   * to CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT, [ProgressDatabaseFullDialogFragment] will be
   * displayed to the user. Otherwise, the dialog fragment [UnsavedExplorationDialogFragment] will
   * be displayed to the user.
   */
  private fun showDialogFragmentBasedOnCurrentCheckpointState() {
    val checkpointState = getExplorationFragment()?.getExplorationCheckpointState()

    // Show UnsavedExplorationDialogFragment if checkpoint state could not be retrieved.
    if (checkpointState == null) {
      showUnsavedExplorationDialogFragment()
    } else {
      when (checkpointState) {
        CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT -> {
          stopExploration(isCompletion = false)
        }
        CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT -> {
          showProgressDatabaseFullDialogFragment()
        }
        else -> showUnsavedExplorationDialogFragment()
      }
    }
  }
}
