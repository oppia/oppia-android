package org.oppia.android.domain.exploration

import org.oppia.android.app.model.EphemeralExploration
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

private const val GET_EXPLORATION_BY_ID_PROVIDER_ID = "get_exploration_by_id_provider_id"
private const val GET_LOCALIZABLE_EXPLORATION_BY_ID_PROVIDER_ID =
  "get_localizable_exploration_by_id_provider_id"

/**
 * Controller for loading explorations by ID, or beginning to play an exploration. This controller
 * is also responsible for controlling the saved checkpoints when the exploration is started or
 * stopped.
 *
 * At most one exploration may be played at a given time, and its state will be managed by
 * [ExplorationProgressController].
 */
class ExplorationDataController @Inject constructor(
  private val explorationProgressController: ExplorationProgressController,
  private val explorationRetriever: ExplorationRetriever,
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val translationController: TranslationController
) {
  /** Returns an [EphemeralExploration] given an ID. */
  fun getExplorationById(profileId: ProfileId, id: String): DataProvider<EphemeralExploration> {
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    val explorationProvider = dataProviders.createInMemoryDataProviderAsync(
      GET_EXPLORATION_BY_ID_PROVIDER_ID
    ) { retrieveExplorationById(id) }
    return explorationProvider.combineWith(
      translationLocaleProvider, GET_LOCALIZABLE_EXPLORATION_BY_ID_PROVIDER_ID
    ) { exploration, locale -> exploration.toEphemeral(locale) }
  }

  /**
   * Begins playing an exploration of the specified ID.
   *
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load
   * success/failure of the exploration.
   *
   * This can be called even if a session is currently active as it will force initiate a new play
   * session, resetting any data from the previous session (though any pending unsaved checkpoint
   * progress is guaranteed to be saved from the previous session, first).
   *
   * [stopPlayingExploration] may be optionally called to clean up the session--see the
   * documentation for that method for details.
   *
   * Note that this method is specifically meant to only be called for explorations which have never
   * been played by the current user before, as it will not resume any saved progress checkpoints,
   * and it will save the user's progress. See [resumeExploration], [restartExploration], and
   * [replayExploration] for other situations.
   *
   * @param internalProfileId the ID corresponding to the profile for which exploration is to be
   *     played
   * @param topicId the ID corresponding to the topic for which exploration has to be played
   * @param storyId the ID corresponding to the story for which exploration has to be played
   * @param explorationId the ID of the exploration which has to be played
   * @return a [DataProvider] to observe whether initiating the play request succeeded
   */
  fun startPlayingNewExploration(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ): DataProvider<Any?> {
    return startPlayingExploration(
      internalProfileId,
      classroomId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance(),
      isRestart = false
    )
  }

  /**
   * Resumes the specified exploration indicated by [topicId], [storyId], and [explorationId] for
   * the user corresponding to [internalProfileId] by restoring the provided
   * [explorationCheckpoint], and returns a [DataProvider] tracking whether the start succeeded.
   *
   * This method behaves the same as [startPlayingNewExploration] except it resumes a previous
   * session. All progress that the user makes during this session will be recorded.
   *
   * This method should generally be called when a user wants to play a lesson for which they have
   * saved progress (unless they want to start over in which case [restartExploration] should be
   * used).
   */
  fun resumeExploration(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    return startPlayingExploration(
      internalProfileId,
      classroomId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = true,
      explorationCheckpoint,
      isRestart = false
    )
  }

  /**
   * Restarts the specified exploration indicated by [topicId], [storyId], and [explorationId] for
   * the user corresponding to [internalProfileId], and returns a [DataProvider] tracking whether
   * the start succeeded.
   *
   * This method behaves the same as [resumeExploration] except any prior progress the user might
   * had for the lesson is dropped and overwritten by any new progress that they achieve.
   *
   * This method should only be used when a user has saved lesson progress and wishes to restart the
   * lesson (otherwise [resumeExploration] should be used to resume the lesson).
   */
  fun restartExploration(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ): DataProvider<Any?> {
    return startPlayingExploration(
      internalProfileId,
      classroomId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = true, // Implied since only checkpoints can be restarted.
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance(),
      isRestart = true
    )
  }

  /**
   * Replays the specified exploration indicated by [topicId], [storyId], and [explorationId] for
   * the user corresponding to [internalProfileId], and returns a [DataProvider] tracking whether
   * the start succeeded.
   *
   * This method behaves the same as [startPlayingNewExploration] except no progress is tracked
   * during the lesson. This method is only meant to be used in cases when a user wants to play a
   * lesson but has already completed that lesson (and, since partial progress can't be conveyed in
   * the UI for completed lessons, such lessons do not have their progress retained).
   *
   * This method should only be called when starting a lesson that's been completed, otherwise one
   * of [startPlayingNewExploration], [resumeExploration], or [restartExploration] should be used,\
   * instead, depending on the specific situation.
   */
  fun replayExploration(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ): DataProvider<Any?> {
    return startPlayingExploration(
      internalProfileId,
      classroomId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress = false, // Finished lessons can't be partially saved.
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance(),
      isRestart = false
    )
  }

  /**
   * Begins playing an exploration of the specified ID.
   *
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load
   * success/failure of the exploration.
   *
   * This can be called even if a session is currently active as it will force initiate a new play
   * session, resetting any data from the previous session (though any pending unsaved checkpoint
   * progress is guaranteed to be saved from the previous session, first).
   *
   * [stopPlayingExploration] may be optionally called to clean up the session--see the
   * documentation for that method for details.
   *
   * @param internalProfileId the ID corresponding to the profile for which exploration has to be
   *     played
   * @param topicId the ID corresponding to the topic for which exploration has to be played
   * @param storyId the ID corresponding to the story for which exploration has to be played
   * @param explorationId the ID of the exploration which has to be played
   * @param shouldSavePartialProgress indicates if partial progress should be saved for the new play
   *     session
   * @param explorationCheckpoint the checkpoint which may be used to resume the exploration
   * @param isRestart whether starting this exploration is erasing a previous checkpoint. In cases
   *     where this is ``true``, [explorationCheckpoint] is expected to be the default proto
   *     instance.
   * @return a [DataProvider] to observe whether initiating the play request, or future play
   *     requests, succeeded
   */
  private fun startPlayingExploration(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint,
    isRestart: Boolean
  ): DataProvider<Any?> {
    return explorationProgressController.beginExplorationAsync(
      ProfileId.newBuilder().apply { loggedInInternalProfileId = internalProfileId }.build(),
      classroomId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress,
      explorationCheckpoint,
      isRestart
    )
  }

  /**
   * Finishes the most recent exploration started by [startPlayingNewExploration],
   * [resumeExploration], [restartExploration], or [replayExploration], and returns a [DataProvider]
   * indicating whether the operation succeeded.
   *
   * This method should only be called if an active exploration is being played, otherwise the
   * resulting provider will fail. Note that this doesn't actually need to be called between
   * sessions unless the caller wants to ensure other providers monitored from
   * [ExplorationProgressController] are reset to a proper out-of-session state.
   *
   * @param isCompletion indicates whether this stop action is fully ending the exploration (i.e. no
   *     checkpoint will be saved since this indicates the exploration is completed)
   */
  fun stopPlayingExploration(isCompletion: Boolean): DataProvider<Any?> =
    explorationProgressController.finishExplorationAsync(isCompletion)

  /**
   * Fetches the details of the oldest saved exploration for a specified profileId.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @return a [DataProvider] that indicates the success or failure of the retrieve operation
   */
  fun getOldestExplorationDetailsDataProvider(profileId: ProfileId) =
    explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(profileId)

  /**
   * Kicks off the operation to delete the saved progress for the exploration specified by the
   * exploration id and profile id.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @param explorationId the ID of the exploration whose checkpoint has to be deleted
   */
  fun deleteExplorationProgressById(profileId: ProfileId, explorationId: String) {
    explorationCheckpointController.deleteSavedExplorationCheckpoint(
      profileId,
      explorationId
    )
  }

  // DataProviders expects this function to be a suspend function.
  @Suppress("RedundantSuspendModifier")
  private suspend fun retrieveExplorationById(explorationId: String): AsyncResult<Exploration> {
    return try {
      AsyncResult.Success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }

  private fun Exploration.toEphemeral(
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralExploration {
    return EphemeralExploration.newBuilder().apply {
      exploration = this@toEphemeral
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          exploration.writtenTranslationsMap, contentLocale
        )
    }.build()
  }
}
