package org.oppia.android.app.survey

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val BEGIN_SESSION_RESULT_PROVIDER_ID = "SurveyProgressController.begin_session_result"

/**
 * A default session ID to be used before a session has been initialized.
 *
 * This session ID will never match, so messages that are received with this ID will never be
 * processed.
 */
private const val DEFAULT_SESSION_ID = "default_session_id"

/**
 * Controller for tracking the non-persisted progress of a survey.
 */
@Singleton
class SurveyProgressController @Inject constructor(
  private val dataProviders: DataProviders,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  private var mostRecentSessionId: String? = null
  private val activeSessionId: String
    get() = mostRecentSessionId ?: DEFAULT_SESSION_ID

  private var mostRecentEphemeralQuestionFlow =
    createAsyncResultStateFlow<EphemeralSurveyQuestion>(
      AsyncResult.Failure(IllegalStateException("Survey is not yet initialized."))
    )

  private var mostRecentCommandQueue: SendChannel<ControllerMessage<*>>? = null

  /**
   * Begins a survey session based on a set of questions and returns a [DataProvider] indicating
   * whether the start was successful.
   */
  internal fun beginSurveySession(profileId: ProfileId): DataProvider<Any?> {
    val ephemeralQuestionFlow = createAsyncResultStateFlow<EphemeralSurveyQuestion>()
    val sessionId = UUID.randomUUID().toString().also {
      mostRecentSessionId = it
      mostRecentEphemeralQuestionFlow = ephemeralQuestionFlow
      mostRecentCommandQueue = createControllerCommandActor()
    }

    val beginSessionResultFlow = createAsyncResultStateFlow<Any?>()
    val initializeMessage: ControllerMessage<*> =
      ControllerMessage.InitializeController(
        profileId, ephemeralQuestionFlow, sessionId, beginSessionResultFlow
      )
    sendCommandForOperation(initializeMessage) {
      "Failed to schedule command for initializing the question assessment progress controller."
    }
    return beginSessionResultFlow.convertToSessionProvider(BEGIN_SESSION_RESULT_PROVIDER_ID)
  }

  private fun createControllerCommandActor(): SendChannel<ControllerMessage<*>> {
    // lateinit var controllerState: ControllerState

    @Suppress("JoinDeclarationAndAssignment") // Warning is incorrect in this case.
    lateinit var commandQueue: SendChannel<ControllerMessage<*>>
    commandQueue = CoroutineScope(
      backgroundCoroutineDispatcher
    ).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        when (message) {
          is ControllerMessage.InitializeController -> {
          }
          is ControllerMessage.FinishSurveySession -> TODO()
          is ControllerMessage.MoveToNextQuestion -> TODO()
          is ControllerMessage.MoveToPreviousQuestion -> TODO()
          is ControllerMessage.RecomputeStateAndNotify -> TODO()
          is ControllerMessage.SaveFullCompletion -> TODO()
          is ControllerMessage.SavePartialCompletion -> TODO()
          is ControllerMessage.SubmitAnswer -> TODO()
        }
      }
    }
    return commandQueue
  }

  private fun <T> sendCommandForOperation(
    message: ControllerMessage<T>,
    lazyFailureMessage: () -> String
  ) {
    // TODO(#4119): Switch this to use trySend(), instead, which is much cleaner and doesn't require
    //  catching an exception.
    val flowResult: AsyncResult<T> = try {
      val commandQueue = mostRecentCommandQueue
      when {
        commandQueue == null ->
          AsyncResult.Failure(IllegalStateException("Session isn't initialized yet."))
        !commandQueue.offer(message) ->
          AsyncResult.Failure(IllegalStateException(lazyFailureMessage()))
        // Ensure that the result is first reset since there will be a delay before the message is
        // processed (if there's a flow).
        else -> AsyncResult.Pending()
      }
    } catch (e: Exception) {
      AsyncResult.Failure(e)
    }
    // This must be assigned separately since flowResult should always be calculated, even if
    // there's no callbackFlow to report it.
    message.callbackFlow?.value = flowResult
  }

  private fun <T> createAsyncResultStateFlow(initialValue: AsyncResult<T> = AsyncResult.Pending()) =
    MutableStateFlow(initialValue)

  private fun <T> StateFlow<AsyncResult<T>>.convertToSessionProvider(
    baseId: String
  ): DataProvider<T> = dataProviders.run {
    convertAsyncToAutomaticDataProvider("${baseId}_$activeSessionId")
  }

  /**
   * Represents a message that can be sent to [mostRecentCommandQueue] to process changes to
   * [ControllerState] (since all changes must be synchronized).
   *
   * Messages are expected to be resolved serially (though their scheduling can occur across
   * multiple threads, so order cannot be guaranteed until they're enqueued).
   */
  private sealed class ControllerMessage<T> {
    /**
     * The session ID corresponding to this message (the message is expected to be ignored if it
     * doesn't correspond to an active session).
     */
    abstract val sessionId: String

    /**
     * The [DataProvider]-tied [MutableStateFlow] that represents the result of the operation
     * corresponding to this message, or ``null`` if the caller doesn't care about observing the
     * result.
     */
    abstract val callbackFlow: MutableStateFlow<AsyncResult<T>>?

    /** [ControllerMessage] for initializing a new survey session. */
    data class InitializeController(
      val profileId: ProfileId,
      val ephemeralQuestionFlow: MutableStateFlow<AsyncResult<EphemeralSurveyQuestion>>,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for ending the current survey session. */
    data class FinishSurveySession(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] for submitting a new [SurveySelectedAnswer]. */
    data class SubmitAnswer(
      val selectedAnswer: SurveySelectedAnswer,
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the previous question in the survey. */
    data class MoveToPreviousQuestion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /** [ControllerMessage] to move to the next question in the survey. */
    data class MoveToNextQuestion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to indicate that the mandatory part of the survey is completed and
     * should be saved/submitted.
     * TODO: remove the comment on the next line
     * Maybe we can use this information to notify some subscriber that a survey can be submitted
     * if an exit action is triggered
     */
    data class SavePartialCompletion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] to indicate that the optional part of the survey is completed and
     * should be saved/submitted.
     */
    data class SaveFullCompletion(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()

    /**
     * [ControllerMessage] which recomputes the current [EphemeralQuestion] and notifies subscribers
     * of the [DataProvider] returned by [getCurrentQuestion] of the change.
     * This is only used in cases where an external operation trigger changes that are only
     * reflected when recomputing the question (e.g. a new answer was selected after initial save).
     */
    data class RecomputeStateAndNotify(
      override val sessionId: String,
      override val callbackFlow: MutableStateFlow<AsyncResult<Any?>>? = null
    ) : ControllerMessage<Any?>()
  }
}
