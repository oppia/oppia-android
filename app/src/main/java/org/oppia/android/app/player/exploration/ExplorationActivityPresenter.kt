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
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.stopplaying.ProgressDatabaseFullDialogFragment
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.UnsavedExplorationDialogFragment
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ExplorationActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

const val TAG_EXPLORATION_FRAGMENT = "TAG_EXPLORATION_FRAGMENT"
const val TAG_EXPLORATION_MANAGER_FRAGMENT = "TAG_EXPLORATION_MANAGER_FRAGMENT"
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

  private var oldestExplorationId: String? = null
  private var oldestExplorationTitle: String? = null

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
      binding.explorationToolbarTitle.isSelected = true
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

    // Retrieve oldest save checkpoint details.
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

  fun deleteCurrentProgressAndStopExploration() {
    explorationDataController.deleteExplorationProgressById(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      explorationId
    )
    stopExploration()
  }

  fun deleteOldestSavedProgressAndStopExploration() {
    // If the value of oldestExplorationId is null, it means that there was an error while
    // retrieving the oldest saved checkpoint details. In this case, the exploration is exited
    // without deleting the any checkpoints.
    if (oldestExplorationId != null) {
      explorationDataController.deleteExplorationProgressById(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        oldestExplorationId!!
      )
    }
    stopExploration()
  }

  fun stopExploration() {
    fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE.name)
    explorationDataController.stopPlayingExploration()
      .observe(
        activity,
        Observer<AsyncResult<Any?>> {
          when {
            it.isPending() -> oppiaLogger.d("ExplorationActivity", "Stopping exploration")
            it.isFailure() -> oppiaLogger.e(
              "ExplorationActivity",
              "Failed to stop exploration",
              it.getErrorOrNull()!!
            )
            else -> {
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

  fun backButtonPressed() {
    // If checkpointing is not enabled, show StopExplorationDialogFragment to exit the exploration,
    // this is expected to happen if the exploration is marked as completed.
    if (!isCheckpointingEnabled) {
      showUnsavedExplorationDialogFragment()
      return
    }
    // If checkpointing is enabled, get the current checkpoint state to figure out the if
    // so far checkpointing has been successful in the exploration.
    subscribeToCheckpointState(explorationDataController.checkHasCheckpointingBeenSuccessful())
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
    if (ephemeralStateResult.isFailure()) {
      oppiaLogger.e(
        "ExplorationActivity",
        "Failed to retrieve answer outcome",
        ephemeralStateResult.getErrorOrNull()!!
      )
    }
    return ephemeralStateResult.getOrDefault(Exploration.getDefaultInstance())
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

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    val explorationFragment =
      activity.supportFragmentManager.findFragmentByTag(
        TAG_EXPLORATION_FRAGMENT
      ) as ExplorationFragment
    explorationFragment.revealHint(saveUserChoice, hintIndex)
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

    // if the value of oldestExplorationId is null, it means that there was an error while
    // retrieving the oldest saved checkpoint details. In that the exploration is exited without
    // deleting the any checkpoints.
    if (oldestExplorationId == null || oldestExplorationTitle == null) {
      stopExploration()
      return
    }

    val dialogFragment =
      ProgressDatabaseFullDialogFragment.newInstance(oldestExplorationTitle!!)
    dialogFragment.showNow(
      activity.supportFragmentManager,
      TAG_PROGRESS_DATABASE_FULL_DIALOG
    )
  }

  private fun showStopExplorationDialogFragment() {
    val previousFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_STOP_EXPLORATION_DIALOG)
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = StopExplorationDialogFragment.newInstance()
    dialogFragment.showNow(activity.supportFragmentManager, TAG_STOP_EXPLORATION_DIALOG)
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
   * This function listens to the result of [ExplorationDataController.getExplorationById].
   *
   * If the result is success it updates the value of the variables oldestExplorationId and
   * oldestExplorationTitle. If the result fails, it does not change the values of the variables
   * oldestExplorationId and oldestExplorationTitle and they remain equal null.
   *
   * Since this function is kicked off before any other save operation, therefore it is expected
   * to complete before any following save operation completes.
   *
   * If operations fails or this function does not get enough time to complete, user is not blocked
   * instead the flow of the application proceeds as if the checkpoints were not found. In that case,
   * the values of the variables oldestExplorationId and oldestExplorationTitle is not changed and
   * they remain equal to null.
   */
  private fun subscribeToOldestSavedExplorationDetails() {
    explorationDataController.getOldestExplorationDetailsDataProvider(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    ).toLiveData().observe(
      activity,
      Observer {
        if (it.isSuccess()) {
          oldestExplorationId = it.getOrThrow().explorationId
          oldestExplorationTitle = it.getOrThrow().explorationTitle
        } else if (it.isFailure()) {
          oppiaLogger.e(
            "Exploration Activity",
            "Failed to retrieve oldest saved checkpoint details.",
            it.getErrorOrNull()
          )
        }
      }
    )
  }

  /**
   * This function listens to the result of the function
   * [ExplorationDataController.checkHasCheckpointingBeenSuccessful]
   *
   * Once the result is available this functions  shows an appropriate dialog box.
   *
   * If a success result returned by the function
   * [ExplorationDataController.checkHasCheckpointingBeenSuccessful], this functions displays the
   * [StopExplorationDialogFragment].
   *
   * If case of a failure result, if the exception is
   * [ExplorationProgressController.ProgressNotSavedException],
   * [UnsavedExplorationDialogFragment] is displayed.
   * If the exception is [ExplorationProgressController.CheckpointDatabaseOverflowException],
   * [ProgressDatabaseFullDialogFragment] is displayed.
   */
  private fun subscribeToCheckpointState(checkpointStateLiveData: LiveData<AsyncResult<Any?>>) {
    checkpointStateLiveData.observe(
      activity,
      Observer {
        if (it.isSuccess()) {
          showStopExplorationDialogFragment()
        } else if (it.isFailure()) {
          when (it.getErrorOrNull()) {
            is ExplorationProgressController.ProgressNotSavedException -> {
              showUnsavedExplorationDialogFragment()
            }
            is ExplorationProgressController.CheckpointDatabaseOverflowException -> {
              showProgressDatabaseFullDialogFragment()
            }
          }
        }
      }
    )
  }
}
