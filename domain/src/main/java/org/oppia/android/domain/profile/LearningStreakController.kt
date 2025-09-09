package org.oppia.android.domain.profile

import org.oppia.android.app.model.LearningStreak
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_LEARNING_STREAK_PROVIDER_ID = "get_learning_streak_provider_id"
private const val UPDATE_LEARNING_STREAK_PROVIDER_ID = "update_learning_streak_provider_id"
private const val RECORD_LEARNING_SESSION_PROVIDER_ID = "record_learning_session_provider_id"

// Number of milliseconds in a day
private const val MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000L

/**
 * Controller for managing user learning streaks. Tracks consecutive days of learning activity,
 * manages streak milestones, and provides streak-related data for UI display.
 */
@Singleton
class LearningStreakController @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val dataProviders: DataProviders,
  private val oppiaClock: OppiaClock,
  private val oppiaLogger: OppiaLogger
) {

  /**
   * Returns the learning streak data for the specified profile.
   *
   * @param profileId the ID of the profile whose streak data to retrieve
   * @return a [DataProvider] containing the learning streak data
   */
  fun getLearningStreak(profileId: ProfileId): DataProvider<LearningStreak> {
    return profileManagementController.getProfile(profileId).transformAsync(
      GET_LEARNING_STREAK_PROVIDER_ID
    ) { profile ->
      val learningStreak = profile.learningStreak.takeIf { 
        it != LearningStreak.getDefaultInstance() 
      } ?: createDefaultLearningStreak()
      
      AsyncResult.Success(learningStreak)
    }
  }

  /**
   * Records a learning session for the specified profile. This updates the learning streak
   * if the session represents a new day of learning activity.
   *
   * @param profileId the ID of the profile to record the session for
   * @return a [DataProvider] indicating success or failure of the operation
   */
  fun recordLearningSession(profileId: ProfileId): DataProvider<LearningStreakResult> {
    return dataProviders.createInMemoryDataProviderAsync(RECORD_LEARNING_SESSION_PROVIDER_ID) {
      try {
        val profile = profileManagementController.getProfile(profileId).value
        if (profile is AsyncResult.Success) {
          val currentStreak = profile.value.learningStreak.takeIf {
            it != LearningStreak.getDefaultInstance()
          } ?: createDefaultLearningStreak()

          val now = oppiaClock.getCurrentTimeMs()
          val updatedStreak = processLearningSession(currentStreak, now)
          
          // Update the profile with the new streak data
          updateProfileLearningStreak(profileId, updatedStreak).await()
          
          AsyncResult.Success(
            LearningStreakResult(
              updatedStreak = updatedStreak,
              isNewStreakRecord = updatedStreak.currentStreakCount > currentStreak.longestStreakCount,
              isStreakMaintained = updatedStreak.currentStreakCount > 0,
              previousStreak = currentStreak.currentStreakCount
            )
          )
        } else {
          AsyncResult.Failure(Exception("Failed to get profile"))
        }
      } catch (e: Exception) {
        oppiaLogger.e("LearningStreakController", "Failed to record learning session", e)
        AsyncResult.Failure(e)
      }
    }
  }

  /**
   * Checks if a learning session should be recorded based on the time since the last activity.
   * This is useful for preventing multiple sessions from being recorded on the same day.
   *
   * @param profileId the ID of the profile to check
   * @return true if a new session should be recorded, false otherwise
   */
  suspend fun shouldRecordLearningSession(profileId: ProfileId): Boolean {
    return try {
      val profile = profileManagementController.getProfile(profileId).value
      if (profile is AsyncResult.Success) {
        val learningStreak = profile.value.learningStreak.takeIf {
          it != LearningStreak.getDefaultInstance()
        } ?: return true // First session should always be recorded

        val now = oppiaClock.getCurrentTimeMs()
        val lastActivity = learningStreak.lastActivityTimestampMs
        
        // Check if it's a different day since last activity
        !isSameDay(lastActivity, now)
      } else {
        true // If we can't get the profile, err on the side of recording
      }
    } catch (e: Exception) {
      oppiaLogger.e("LearningStreakController", "Failed to check if session should be recorded", e)
      true // If there's an error, record the session
    }
  }

  /**
   * Processes a learning session and returns the updated streak data.
   */
  private fun processLearningSession(
    currentStreak: LearningStreak,
    sessionTimestamp: Long
  ): LearningStreak {
    val lastActivityTime = currentStreak.lastActivityTimestampMs
    
    // If this is the first session ever
    if (lastActivityTime == 0L) {
      return LearningStreak.newBuilder()
        .setCurrentStreakCount(1)
        .setLongestStreakCount(1)
        .setLastActivityTimestampMs(sessionTimestamp)
        .setCurrentStreakStartTimestampMs(sessionTimestamp)
        .setTotalLearningSessions(1)
        .build()
    }

    // Check if it's the same day - don't update streak for same day
    if (isSameDay(lastActivityTime, sessionTimestamp)) {
      return currentStreak.toBuilder()
        .setLastActivityTimestampMs(sessionTimestamp)
        .setTotalLearningSessions(currentStreak.totalLearningSessions + 1)
        .build()
    }

    // Check if it's the next consecutive day
    if (isConsecutiveDay(lastActivityTime, sessionTimestamp)) {
      val newStreakCount = currentStreak.currentStreakCount + 1
      val newLongestStreak = maxOf(newStreakCount, currentStreak.longestStreakCount)
      
      return currentStreak.toBuilder()
        .setCurrentStreakCount(newStreakCount)
        .setLongestStreakCount(newLongestStreak)
        .setLastActivityTimestampMs(sessionTimestamp)
        .setTotalLearningSessions(currentStreak.totalLearningSessions + 1)
        .build()
    } else {
      // Streak is broken, start a new one
      return currentStreak.toBuilder()
        .setCurrentStreakCount(1)
        .setLastActivityTimestampMs(sessionTimestamp)
        .setCurrentStreakStartTimestampMs(sessionTimestamp)
        .setTotalLearningSessions(currentStreak.totalLearningSessions + 1)
        .build()
    }
  }

  /**
   * Updates the learning streak data for a profile.
   */
  private suspend fun updateProfileLearningStreak(
    profileId: ProfileId,
    learningStreak: LearningStreak
  ) {
    profileManagementController.updateLearningStreak(profileId, learningStreak).value
  }

  /**
   * Creates a default learning streak for new profiles.
   */
  private fun createDefaultLearningStreak(): LearningStreak {
    return LearningStreak.newBuilder()
      .setCurrentStreakCount(0)
      .setLongestStreakCount(0)
      .setLastActivityTimestampMs(0)
      .setCurrentStreakStartTimestampMs(0)
      .setTotalLearningSessions(0)
      .build()
  }

  /**
   * Checks if two timestamps are on the same day.
   */
  private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val day1 = timestamp1 / MILLISECONDS_IN_DAY
    val day2 = timestamp2 / MILLISECONDS_IN_DAY
    return day1 == day2
  }

  /**
   * Checks if the second timestamp is the day immediately following the first timestamp.
   */
  private fun isConsecutiveDay(previousTimestamp: Long, currentTimestamp: Long): Boolean {
    val previousDay = previousTimestamp / MILLISECONDS_IN_DAY
    val currentDay = currentTimestamp / MILLISECONDS_IN_DAY
    return currentDay == previousDay + 1
  }
}

/**
 * Represents the result of recording a learning session.
 */
data class LearningStreakResult(
  val updatedStreak: LearningStreak,
  val isNewStreakRecord: Boolean,
  val isStreakMaintained: Boolean,
  val previousStreak: Int
)
