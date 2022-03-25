package org.oppia.android.domain.oppialogger.analytics

import com.google.protobuf.MessageLite
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.Context as EventContext
import org.oppia.android.app.model.EventLog.Context.Builder as EventBuilder
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.PlayVoiceOverContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.State
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger.BaseLogger.Companion.maybeLogEvent

@Singleton
class LearnerAnalyticsLogger @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val loggingIdentifierController: LoggingIdentifierController
) {
  val explorationAnalyticsLogger by lazy { mutableExpAnalyticsLogger.asStateFlow() }

  private val mutableExpAnalyticsLogger = MutableStateFlow<ExplorationAnalyticsLogger?>(null)

  fun beginExploration(
    installationId: String?,
    learnerId: String?,
    exploration: Exploration,
    checkpoint: ExplorationCheckpoint,
    topicId: String,
    storyId: String
  ): ExplorationAnalyticsLogger {
    val startingStateName = if (checkpoint != ExplorationCheckpoint.getDefaultInstance()) {
      checkpoint.pendingStateName
    } else exploration.initStateName

    return ExplorationAnalyticsLogger(
      installationId,
      learnerId,
      topicId,
      storyId,
      exploration.id,
      exploration.version.toString(),
      startingStateName,
      oppiaLogger,
      loggingIdentifierController
    ).also {
      if (!mutableExpAnalyticsLogger.compareAndSet(expect = null, update = it)) {
        oppiaLogger.e(
          "LearnerAnalyticsLogger",
          "Attempting to start an exploration without ending the previous."
        )

        // Force the logger to a new state.
        mutableExpAnalyticsLogger.value = it
      }
    }
  }

  fun endExploration() {
    if (mutableExpAnalyticsLogger.value == null) {
      oppiaLogger.e(
        "LearnerAnalyticsLogger", "Attempting to end an exploration that hasn't been started."
      )
    }
    mutableExpAnalyticsLogger.value = null
  }

  fun logAppInBackground(installationId: String?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    oppiaLogger.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setAppInBackgroundContext)
    )
  }

  fun logAppInForeground(installationId: String?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    oppiaLogger.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setAppInForegroundContext)
    )
  }

  fun logDeleteProfile(installationId: String?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    oppiaLogger.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setDeleteProfileContext)
    )
  }

  class ExplorationAnalyticsLogger internal constructor(
    installationId: String?,
    learnerId: String?,
    topicId: String,
    storyId: String,
    explorationId: String,
    explorationVersion: String,
    startingStateName: String,
    private val oppiaLogger: OppiaLogger,
    private val loggingIdentifierController: LoggingIdentifierController
  ) {
    val stateAnalyticsLogger by lazy { mutableStateAnalyticsLogger.asStateFlow() }

    private val mutableStateAnalyticsLogger = MutableStateFlow<StateAnalyticsLogger?>(null)

    private val baseLogger by lazy { BaseLogger(oppiaLogger, installationId) }
    private val learnerDetailsContext by lazy {
      createLearnerDetailsContext(installationId, learnerId)
    }
    private val explorationLogContext by lazy {
      learnerDetailsContext?.let { learnerDetails ->
        val learnerSessionId = loggingIdentifierController.getSessionIdFlow().value
        ExplorationContext.newBuilder().apply {
          sessionId = learnerSessionId
          this.explorationId = explorationId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationVersion = explorationVersion
          stateName = startingStateName
          this.learnerDetails = learnerDetails
        }.build()
      }?.ensureNonEmpty()
    }

    fun logExitExploration() {
      getExpectedStateLogger()?.logExitExploration()
    }

    fun logFinishExploration() {
      getExpectedStateLogger()?.logFinishExploration()
    }

    fun logResumeExploration() {
      baseLogger.maybeLogLearnerEvent(learnerDetailsContext) {
        createAnalyticsEvent(it, EventBuilder::setResumeExplorationContext)
      }
    }

    fun logStartExplorationOver() {
      baseLogger.maybeLogLearnerEvent(learnerDetailsContext) {
        createAnalyticsEvent(it, EventBuilder::setStartOverExplorationContext)
      }
    }

    fun startCard(newState: State): StateAnalyticsLogger {
      return StateAnalyticsLogger(
        loggingIdentifierController, baseLogger, newState, explorationLogContext
      ).also {
        if (!mutableStateAnalyticsLogger.compareAndSet(expect = null, update = it)) {
          oppiaLogger.e(
            "LearnerAnalyticsLogger", "Attempting to start a card without ending the previous."
          )

          // Force the logger to a new state.
          mutableStateAnalyticsLogger.value = it
        }
      }
    }

    fun endCard() {
      getExpectedStateLogger() // Verifies that it's set, otherwise logs an error.
      mutableStateAnalyticsLogger.value = null
    }

    private fun getExpectedStateLogger(): StateAnalyticsLogger? {
      return mutableStateAnalyticsLogger.value.also {
        if (it == null) {
          oppiaLogger.e("LearnerAnalyticsLogger", "Attempting to log a state event outside state.")
        }
      }
    }
  }

  class StateAnalyticsLogger internal constructor(
    private val loggingIdentifierController: LoggingIdentifierController,
    private val baseLogger: BaseLogger,
    private val currentState: State,
    private val baseExplorationLogContext: ExplorationContext?
  ) {
    private val linkedSkillId by lazy { currentState.linkedSkillId }

    internal fun logExitExploration() {
      logStateEvent(EventBuilder::setExitExplorationContext)
    }

    internal fun logFinishExploration() {
      logStateEvent(EventBuilder::setFinishExplorationContext)
    }

    fun logStartCard() {
      logStateEvent(linkedSkillId, ::createCardContext, EventBuilder::setStartCardContext)
    }

    fun logEndCard() {
      logStateEvent(linkedSkillId, ::createCardContext, EventBuilder::setEndCardContext)
    }

    fun logHintOffered(hintIndex: Int) {
      logStateEvent(hintIndex, ::createHintContext, EventBuilder::setHintOfferedContext)
    }

    fun logViewHint(hintIndex: Int) {
      logStateEvent(hintIndex, ::createHintContext, EventBuilder::setAccessHintContext)
    }

    fun logSolutionOffered() {
      logStateEvent(EventBuilder::setSolutionOfferedContext)
    }

    fun logViewSolution() {
      logStateEvent(EventBuilder::setAccessSolutionContext)
    }

    fun logSubmitAnswer(isCorrect: Boolean) {
      logStateEvent(isCorrect, ::createSubmitAnswerContext, EventBuilder::setSubmitAnswerContext)
    }

    fun logPlayVoiceOver(contentId: String?) {
      logStateEvent(contentId, ::createPlayVoiceOverContext, EventBuilder::setPlayVoiceOverContext)
    }

    private fun logStateEvent(setter: EventBuilder.(ExplorationContext) -> EventBuilder) =
      logStateEvent(Unit, { _, context -> context }, setter)

    private fun <D, T> logStateEvent(
      detail: D,
      baseContextFactory: (D, ExplorationContext) -> T,
      setter: EventBuilder.(T) -> EventBuilder
    ) {
      // The nullness checks here prevent an empty log from getting sent (since that'd be mostly
      // useless), but it does allow for the log-specific data to be absent so long as the context
      // is present.
      baseLogger.maybeLogEvent(
        computeLogContext()?.let {
          createAnalyticsEvent(baseContextFactory(detail, it), setter)
        }?.ensureNonEmpty()
      )
    }

    private fun computeLogContext(): ExplorationContext? {
      return baseExplorationLogContext?.toBuilder()?.apply {
        stateName = currentState.name
        learnerDetails = learnerDetails.toBuilder().apply {
          // Ensure the session ID is the latest for this event (in case it's changed).
          sessionId = loggingIdentifierController.getSessionIdFlow().value
        }.build()
      }?.build()
    }
  }

  internal class BaseLogger internal constructor(
    private val oppiaLogger: OppiaLogger, private val installationId: String?
  ) {
    fun maybeLogLearnerEvent(
      learnerDetailsContext: LearnerDetailsContext?,
      creationDelegate: (LearnerDetailsContext) -> EventContext
    ) {
      // Note that this tries to ensure that the event is always logged even if details are missing.
      maybeLogEvent(
        creationDelegate(learnerDetailsContext ?: LearnerDetailsContext.getDefaultInstance())
      )
    }

    fun maybeLogEvent(context: EventContext?) = oppiaLogger.maybeLogEvent(installationId, context)

    internal companion object {
      internal fun OppiaLogger.maybeLogEvent(installationId: String?, context: EventContext?) {
        if (context != null) {
          logImportantEvent(context)
        } else {
          this.e(
            "LearnerAnalyticsLogger",
            "Event is being dropped due to incomplete event (or missing learner ID for profile)"
          )
          logImportantEvent(createFailedToLogLearnerAnalyticsEvent(installationId))
        }
      }
    }
  }

  private companion object {
    private const val DEFAULT_INSTALLATION_ID = "unknown_installation_id"

    private fun <T : MessageLite> T.ensureNonEmpty(): T? =
      takeIf { it != it.defaultInstanceForType }

    private fun createLearnerDetailsContext(
      installationId: String?, learnerId: String?
    ): LearnerDetailsContext? {
      return createLearnerDetailsContextWithIdsPresent(
        installationId?.takeUnless(String::isEmpty), learnerId?.takeUnless(String::isEmpty)
      ).ensureNonEmpty()
    }

    private fun createLearnerDetailsContextWithIdsPresent(
      installationId: String?, learnerId: String?
    ): LearnerDetailsContext {
      return LearnerDetailsContext.newBuilder().apply {
        installationId?.let { this.deviceId = it }
        learnerId?.let { this.learnerId = it }
      }.build()
    }

    private fun createCardContext(
      skillId: String, explorationDetails: ExplorationContext
    ) = CardContext.newBuilder().apply {
      this.skillId = skillId
      this.explorationDetails = explorationDetails
    }.build()

    private fun createHintContext(
      hintIndex: Int, explorationDetails: ExplorationContext
    ) = HintContext.newBuilder().apply {
      this.hintIndex = hintIndex
      this.explorationDetails = explorationDetails
    }.build()

    private fun createSubmitAnswerContext(
      isAnswerCorrect: Boolean, explorationDetails: ExplorationContext
    ) = SubmitAnswerContext.newBuilder().apply {
      this.isAnswerCorrect = isAnswerCorrect
      this.explorationDetails = explorationDetails
    }.build()

    private fun createPlayVoiceOverContext(
      contentId: String?, explorationDetails: ExplorationContext
    ) = PlayVoiceOverContext.newBuilder().apply {
      contentId?.let { this.contentId = it }
      this.explorationDetails = explorationDetails
    }.build()

    private fun <T> createAnalyticsEvent(
      baseContext: T, setter: EventBuilder.(T) -> EventContext.Builder
    ) = EventContext.newBuilder().setter(baseContext).build()

    private fun createFailedToLogLearnerAnalyticsEvent(deviceId: String?): EventContext {
      return EventContext.newBuilder().apply {
        deviceIdForFailedAnalyticsLog = deviceId ?: DEFAULT_INSTALLATION_ID
      }.build()
    }
  }
}
