package org.oppia.android.app.home

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.LearningStreak
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.profile.LearningStreakController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * View model for displaying learning streak information in the home screen.
 * Shows current streak, longest streak, and motivational messages.
 */
@FragmentScope
class LearningStreakViewModel @Inject constructor(
  private val learningStreakController: LearningStreakController,
  private val resourceHandler: AppLanguageResourceHandler
) {

  /** The current learning streak data. */
  private var _learningStreakLiveData: LiveData<LearningStreak> = 
    Transformations.map(
      // This will be updated when we get the profile ID
      learningStreakController.getLearningStreak(
        ProfileId.newBuilder().setInternalId(0).build()
      ).toLiveData()
    ) { result ->
      when (result) {
        is AsyncResult.Success -> result.value
        else -> LearningStreak.getDefaultInstance()
      }
    }

  /** Observable current streak count for data binding. */
  val currentStreak = ObservableField<String>()

  /** Observable longest streak count for data binding. */
  val longestStreak = ObservableField<String>()

  /** Observable streak message for motivation. */
  val streakMessage = ObservableField<String>()

  /** Observable streak icon for visual feedback. */
  val streakIcon = ObservableField<Int>()

  /**
   * Sets the profile ID and updates the learning streak data.
   */
  fun setProfileId(profileId: ProfileId) {
    _learningStreakLiveData = Transformations.map(
      learningStreakController.getLearningStreak(profileId).toLiveData()
    ) { result ->
      when (result) {
        is AsyncResult.Success -> {
          updateStreakDisplay(result.value)
          result.value
        }
        else -> {
          updateStreakDisplay(LearningStreak.getDefaultInstance())
          LearningStreak.getDefaultInstance()
        }
      }
    }
  }

  /**
   * Updates the UI display with the current streak information.
   */
  private fun updateStreakDisplay(learningStreak: LearningStreak) {
    val currentStreakCount = learningStreak.currentStreakCount
    val longestStreakCount = learningStreak.longestStreakCount

    // Update current streak display
    currentStreak.set(
      if (currentStreakCount > 0) {
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.learning_streak_current_count, currentStreakCount.toString()
        )
      } else {
        resourceHandler.getStringInLocale(R.string.learning_streak_start_today)
      }
    )

    // Update longest streak display
    longestStreak.set(
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.learning_streak_longest_count, longestStreakCount.toString()
      )
    )

    // Update motivational message based on streak count
    streakMessage.set(getStreakMessage(currentStreakCount))

    // Update streak icon based on streak level
    streakIcon.set(getStreakIcon(currentStreakCount))
  }

  /**
   * Returns an appropriate motivational message based on the current streak count.
   */
  private fun getStreakMessage(streakCount: Int): String {
    return when {
      streakCount == 0 -> resourceHandler.getStringInLocale(R.string.learning_streak_message_start)
      streakCount == 1 -> resourceHandler.getStringInLocale(R.string.learning_streak_message_first_day)
      streakCount < 7 -> resourceHandler.getStringInLocale(R.string.learning_streak_message_building)
      streakCount < 30 -> resourceHandler.getStringInLocale(R.string.learning_streak_message_strong)
      streakCount < 100 -> resourceHandler.getStringInLocale(R.string.learning_streak_message_amazing)
      else -> resourceHandler.getStringInLocale(R.string.learning_streak_message_legendary)
    }
  }

  /**
   * Returns an appropriate icon resource based on the current streak count.
   */
  private fun getStreakIcon(streakCount: Int): Int {
    return when {
      streakCount == 0 -> R.drawable.ic_streak_start_24dp
      streakCount < 7 -> R.drawable.ic_streak_fire_24dp
      streakCount < 30 -> R.drawable.ic_streak_fire_strong_24dp
      else -> R.drawable.ic_streak_fire_legendary_24dp
    }
  }

  /**
   * Returns whether the learning streak should be visible.
   * We might want to hide it for new users or under certain conditions.
   */
  fun shouldShowStreak(): Boolean {
    return true // For now, always show the streak feature
  }

  /**
   * Returns the current learning streak data as LiveData for observation.
   */
  fun getLearningStreakLiveData(): LiveData<LearningStreak> {
    return _learningStreakLiveData
  }
}
