package org.oppia.android.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
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
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.stopplaying.ProgressDatabaseFullDialogFragment
import org.oppia.android.app.player.stopplaying.UnsavedExplorationDialogFragment
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ExplorationActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
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
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var explorationToolbar: Toolbar
  private lateinit var explorationToolbarTitle: TextView
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var context: Context
  private var backflowScreen: Int? = null

  private var isCheckpointingEnabled: Boolean = false

  private lateinit var oldestCheckpointExplorationId: String
  private lateinit var oldestCheckpointExplorationTitle: String

  enum class ParentActivityForExploration(val value: Int) {
    BACKFLOW_SCREEN_LESSONS(0),
    BACKFLOW_SCREEN_STORY(1);
  }

  private val exploreViewModel by lazy {
    getExplorationViewModel()
  }

  fun handleOnCreate(
    context: Context,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  ) {
    val binding = DataBindingUtil.setContentView<ExplorationActivityBinding>(
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
      binding.marqueeView.startMarquee()
    }

    binding.explorationToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }

    binding.actionAudioPlayer.setOnClickListener {
      getExplorationFragment()?.handlePlayAudio()
    }

    updateToolbarTitle(explorationId)
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.context = context
    this.backflowScreen = backflowScreen
    this.isCheckpointingEnabled = isCheckpointingEnabled

    // Retrieve oldest saved checkpoint details.
    subscribeToOldestSavedExplorationDetails()

    if (getExplorationManagerFragment() == null) {
      val explorationManagerFragment = ExplorationManagerFragment()
      val args = Bundle()
      args.putInt(
        ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
        internalProfileId
      )
      explorationManagerFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        explorationManagerFragment,
        TAG_EXPLORATION_MANAGER_FRAGMENT
      ).commitNow()
    }
  }

  fun loadExplorationFragment(readingTextSize: ReadingTextSize) {
    if (getExplorationFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        ExplorationFragment.newInstance(
          topicId = topicId,
          internalProfileId = internalProfileId,
          storyId = storyId,
          readingTextSize = readingTextSize.name,
          explorationId = explorationId
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
  fun handleOnOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.action_preferences -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity,
          internalProfileId,
          /* isFromNavigationDrawer= */ false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE.name)
        context.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity, internalProfileId,
          /* isFromNavigationDrawer= */false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE.name)
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
    explorationDataController.deleteExplorationProgressById(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      explorationId
    )
    stopExploration(isCompletion)
  }

  /** Deletes the oldest saved checkpoint and then stops the exploration. */
  fun deleteOldestSavedProgressAndStopExploration() {
    // If oldestCheckpointExplorationId is not initialized, it means that there was an error while
    // retrieving the oldest saved checkpoint details. In this case, the exploration is exited
    // without deleting the any checkpoints.
    oldestCheckpointExplorationId.let {
      explorationDataController.deleteExplorationProgressById(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        oldestCheckpointExplorationId
      )
    }
    stopExploration(isCompletion = false)
  }

  fun stopExploration(isCompletion: Boolean) {
    fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE.name)
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
              backPressActivitySelector(backflowScreen)
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
    subscribeToExploration(explorationDataController.getExplorationById(explorationId).toLiveData())
  }

  private fun subscribeToExploration(
    explorationResultLiveData: LiveData<AsyncResult<Exploration>>
  ) {
    val explorationLiveData = getExploration(explorationResultLiveData)
    explorationLiveData.observe(
      activity,
      Observer<Exploration> {
        explorationToolbarTitle.text = it.title
      }
    )
  }

  private fun getExplorationViewModel(): ExplorationViewModel {
    return viewModelProvider.getForActivity(activity, ExplorationViewModel::class.java)
  }

  /** Helper for subscribeToExploration. */
  private fun getExploration(
    exploration: LiveData<AsyncResult<Exploration>>
  ): LiveData<Exploration> {
    return Transformations.map(exploration, ::processExploration)
  }

  /** Helper for subscribeToExploration. */
  private fun processExploration(ephemeralStateResult: AsyncResult<Exploration>): Exploration {
    return when (ephemeralStateResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ExplorationActivity", "Failed to retrieve answer outcome", ephemeralStateResult.error
        )
        Exploration.getDefaultInstance()
      }
      is AsyncResult.Pending -> Exploration.getDefaultInstance()
      is AsyncResult.Success -> ephemeralStateResult.value
    }
  }

  private fun backPressActivitySelector(backflowScreen: Int?) {
    when (backflowScreen) {
      ParentActivityForExploration.BACKFLOW_SCREEN_STORY.value -> activity.finish()
      ParentActivityForExploration.BACKFLOW_SCREEN_LESSONS.value -> activity.finish()
      else -> activity.startActivity(
        TopicActivity.createTopicActivityIntent(
          context,
          internalProfileId,
          topicId
        )
      )
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
    // details. In that case the exploration will be exited without deleting the any checkpoints.
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
   * If the result is success it updates the value of the variables oldestCheckpointExplorationId
   * and oldestCheckpointExplorationTitle. If the result fails, it does not initializes the
   * variables oldestCheckpointExplorationId and oldestCheckpointExplorationTitle with any value.
   *
   * Since this function is kicked off before any other save operation, therefore it is expected
   * to complete before any following save operation completes.
   *
   * If operations fails or this function does not get enough time to complete, user is not blocked
   * instead the flow of the application proceeds as if the checkpoints were not found. In that case,
   * the variables oldestCheckpointExplorationId and oldestCheckpointExplorationTitle are not
   * initialized and they remain uninitialized.
   */
  private fun subscribeToOldestSavedExplorationDetails() {
    explorationDataController.getOldestExplorationDetailsDataProvider(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    ).toLiveData().observe(
      activity,
      Observer {
        when (it) {
          is AsyncResult.Success -> {
            oldestCheckpointExplorationId = it.value.explorationId
            oldestCheckpointExplorationTitle = it.value.explorationTitle
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "ExplorationActivity", "Failed to retrieve oldest saved checkpoint details.", it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for an actual result.
        }
      }
    )
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
