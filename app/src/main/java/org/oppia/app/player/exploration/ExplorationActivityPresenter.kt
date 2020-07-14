package org.oppia.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ExplorationActivityBinding
import org.oppia.app.model.Exploration
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.FontScaleConfigurationUtil
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

const val TAG_EXPLORATION_FRAGMENT = "TAG_EXPLORATION_FRAGMENT"
const val TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER = "HINTS_AND_SOLUTION_EXPLORATION_MANAGER"

/** The Presenter for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ExplorationViewModel>,
  private val logger: ConsoleLogger
) {
  private lateinit var explorationToolbar: Toolbar
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var profileId: ProfileId
  private lateinit var storyTextSize: StoryTextSize
  private lateinit var context: Context
  private var backflowScreen: Int? = null

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
    backflowScreen: Int?
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
    activity.setSupportActionBar(explorationToolbar)

    binding.actionAudioPlayer.setOnClickListener {
      getExplorationFragment()?.handlePlayAudio()
    }

    updateToolbarTitle(explorationId)
    this.context = context
    this.internalProfileId = internalProfileId
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.backflowScreen = backflowScreen
    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    if (getHintsAndSolutionExplorationManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        HintsAndSolutionExplorationManagerFragment()
      ).commitNow()
    }
  }

  private fun getProfileData(profileId: ProfileId): LiveData<StoryTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processGetProfileResult
    )
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): StoryTextSize {
    if (profileResult.isFailure()) {
      logger.e(
        "ExplorationActivity",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance()).storyTextSize
  }

  fun subscribeToAudioLanguageLiveData(profileId: ProfileId) {
    getProfileData(profileId).observe(
      activity, Observer<StoryTextSize> { result ->
        if (getExplorationFragment() == null) {
          val explorationFragment = ExplorationFragment()
          val args = Bundle()
          args.putInt(
            ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
            internalProfileId
          )
          args.putString(ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
          args.putString(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
          args.putString(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_TEXT_SIZE, result.name)
          args.putString(
            ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY,
            explorationId
          )
          explorationFragment.arguments = args
          activity.supportFragmentManager.beginTransaction().add(
            R.id.exploration_fragment_placeholder,
            explorationFragment,
            TAG_EXPLORATION_FRAGMENT
          ).commitNow()
        }
      }
    )
  }

  private fun getHintsAndSolutionExplorationManagerFragment(): HintsAndSolutionExplorationManagerFragment? { // ktlint-disable max-line-length
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
    ) as HintsAndSolutionExplorationManagerFragment?
  }

  fun showAudioButton() = exploreViewModel.showAudioButton.set(true)

  fun hideAudioButton() = exploreViewModel.showAudioButton.set(false)

  fun showAudioStreamingOn() = exploreViewModel.isAudioStreamingOn.set(true)

  fun showAudioStreamingOff() = exploreViewModel.isAudioStreamingOn.set(false)

  fun setAudioBarVisibility(isVisible: Boolean) =
    getExplorationFragment()?.setAudioBarVisibility(isVisible)

  fun scrollToTop() = getExplorationFragment()?.scrollToTop()

  private fun getExplorationFragment(): ExplorationFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_EXPLORATION_FRAGMENT
    ) as ExplorationFragment?
  }

  fun stopExploration() {
    FontScaleConfigurationUtil(activity, StoryTextSize.MEDIUM_TEXT_SIZE.name).adjustFontScale()
    explorationDataController.stopPlayingExploration()
      .observe(
        activity,
        Observer<AsyncResult<Any?>> {
          when {
            it.isPending() -> logger.d("ExplorationActivity", "Stopping exploration")
            it.isFailure() -> logger.e(
              "ExplorationActivity",
              "Failed to stop exploration",
              it.getErrorOrNull()!!
            )
            else -> {
              logger.d("ExplorationActivity", "Successfully stopped exploration")
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

  private fun updateToolbarTitle(explorationId: String) {
    subscribeToExploration(explorationDataController.getExplorationById(explorationId))
  }

  private fun subscribeToExploration(
    explorationResultLiveData: LiveData<AsyncResult<Exploration>>
  ) {
    val explorationLiveData = getExploration(explorationResultLiveData)
    explorationLiveData.observe(
      activity,
      Observer<Exploration> {
        explorationToolbar.title = it.title
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
      logger.e(
        "StateFragment",
        "Failed to retrieve answer outcome",
        ephemeralStateResult.getErrorOrNull()!!
      )
    }
    return ephemeralStateResult.getOrDefault(Exploration.getDefaultInstance())
  }

  private fun backPressActivitySelector(backflowScreen: Int?) {
    when (backflowScreen) {
      ParentActivityForExploration.BACKFLOW_SCREEN_STORY.value -> activity.startActivity(
        StoryActivity.createStoryActivityIntent(context, internalProfileId, topicId, storyId)
      )
      ParentActivityForExploration.BACKFLOW_SCREEN_LESSONS.value -> activity.startActivity(
        TopicActivity.createTopicPlayStoryActivityIntent(
          activity,
          internalProfileId,
          topicId,
          storyId
        )
      )
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

  fun revealSolution(saveUserChoice: Boolean) {
    val explorationFragment =
      activity.supportFragmentManager.findFragmentByTag(
        TAG_EXPLORATION_FRAGMENT
      ) as ExplorationFragment
    explorationFragment.revealSolution(saveUserChoice)
  }
}
