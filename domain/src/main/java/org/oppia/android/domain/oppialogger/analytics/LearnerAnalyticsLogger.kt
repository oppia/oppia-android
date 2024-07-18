package org.oppia.android.domain.oppialogger.analytics

import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.VoiceoverActionContext
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger.BaseLogger.Companion.maybeLogEvent
import org.oppia.android.util.math.toAnswerString
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.EventLog.Context as EventContext
import org.oppia.android.app.model.EventLog.Context.Builder as EventBuilder

/**
 * Convenience logger for learner-related analytics events.
 *
 * This logger is meant primarily to be used directly in the controller responsible for exploration
 * play session management, but it may be accessed outside that controller but within the same play
 * session scope (e.g. for events that occur outside the core progress state).
 *
 * [beginExploration] can be used to initiate logging for a particular exploration, and
 * [explorationAnalyticsLogger] can be used to access the corresponding [ExplorationAnalyticsLogger]
 * for the current session, if any.
 */
@Singleton
class LearnerAnalyticsLogger @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val loggingIdentifierController: LoggingIdentifierController
) {
  /**
   * The [ExplorationAnalyticsLogger] corresponding to the current play session, or ``null`` if
   * there is no ongoing session.
   */
  val explorationAnalyticsLogger by lazy { mutableExpAnalyticsLogger.asStateFlow() }

  private val mutableExpAnalyticsLogger = MutableStateFlow<ExplorationAnalyticsLogger?>(null)

  /**
   * Starts logging support for a new exploration play session and returns the
   * [ExplorationAnalyticsLogger] corresponding to that session.
   *
   * Calling this function will override the current session logger indicated by
   * [explorationAnalyticsLogger] (and notify any observers of the flow).
   *
   * When the session is over, [endExploration] should be called to make ensure that
   * [explorationAnalyticsLogger] is properly reset.
   *
   * @param installationId the device installation ID corresponding to the new play session, or null
   *     if not known (which may impact whether events are logged)
   * @param profileId the ID of the Oppia profile starting the new exploration session
   * @param learnerId the personal profile/learner ID corresponding to the new session learner, or
   *     null if not known (which may impact whether events are logged)
   * @param exploration the [Exploration] for which a play session is starting
   * @param topicId the ID of the topic to which the story indicated by [storyId] belongs
   * @param storyId the ID of the story to which [exploration] belongs
   */
  fun beginExploration(
    installationId: String?,
    profileId: ProfileId,
    learnerId: String?,
    exploration: Exploration,
    classroomId: String,
    topicId: String,
    storyId: String
  ): ExplorationAnalyticsLogger {
    return ExplorationAnalyticsLogger(
      installationId,
      profileId,
      learnerId,
      classroomId,
      topicId,
      storyId,
      exploration.id,
      exploration.version,
      oppiaLogger,
      analyticsController,
      loggingIdentifierController
    ).also {
      if (!mutableExpAnalyticsLogger.compareAndSet(expect = null, update = it)) {
        oppiaLogger.w(
          "LearnerAnalyticsLogger",
          "Attempting to start an exploration without ending the previous."
        )

        // Force the logger to a new state.
        mutableExpAnalyticsLogger.value = it
      }
    }
  }

  /**
   * Ends the most recent session started by [beginExploration] and resets
   * [explorationAnalyticsLogger].
   */
  fun endExploration() {
    if (mutableExpAnalyticsLogger.value == null) {
      oppiaLogger.w(
        "LearnerAnalyticsLogger", "Attempting to end an exploration that hasn't been started."
      )
    }
    mutableExpAnalyticsLogger.value = null
  }

  /**
   * Logs that the app has entered the background.
   *
   * @param installationId the device installation ID corresponding to the new play session, or null
   *     if not known (which may impact whether the event is logged)
   * @param profileId the ID of the Oppia profile currently logged in, or null if none
   * @param learnerId the personal profile/learner ID corresponding to the new session learner, or
   *     null if not known (which may impact whether the event is logged)
   */
  fun logAppInBackground(installationId: String?, profileId: ProfileId?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    analyticsController.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setAppInBackgroundContext),
      profileId,
      oppiaLogger
    )
  }

  /**
   * Logs that the app has entered the foreground.
   *
   * @param installationId the device installation ID corresponding to the new play session, or null
   *     if not known (which may impact whether the event is logged)
   * @param profileId the ID of the Oppia profile currently logged in, or null if none
   * @param learnerId the personal profile/learner ID corresponding to the new session learner, or
   *     null if not known (which may impact whether the event is logged)
   */
  fun logAppInForeground(installationId: String?, profileId: ProfileId?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    analyticsController.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setAppInForegroundContext),
      profileId,
      oppiaLogger
    )
  }

  /**
   * Logs that the profile corresponding to [learnerId] was deleted from the device and can no
   * longer be used (i.e. no further events will be logged for this profile).
   *
   * @param installationId the device installation ID corresponding to the new play session, or null
   *     if not known (which may impact whether the event is logged)
   * @param profileId the ID of the Oppia profile deleting a profile, or null if no profiles are
   *     currently logged in
   * @param learnerId the personal profile/learner ID corresponding to the new session learner, or
   *     null if not known (which may impact whether the event is logged)
   */
  fun logDeleteProfile(installationId: String?, profileId: ProfileId?, learnerId: String?) {
    val learnerDetailsContext = createLearnerDetailsContextWithIdsPresent(installationId, learnerId)
    analyticsController.maybeLogEvent(
      installationId,
      createAnalyticsEvent(learnerDetailsContext, EventBuilder::setDeleteProfileContext),
      profileId,
      oppiaLogger
    )
  }

  /**
   * Analytics logger for a specific exploration play session.
   *
   * Similar to how this logger has its own lifecycle defined within [LearnerAnalyticsLogger],
   * [stateAnalyticsLogger] provides access to logging state-specific events corresponding to the
   * current active (pending) state, if any.
   */
  class ExplorationAnalyticsLogger internal constructor(
    installationId: String?,
    profileId: ProfileId,
    learnerId: String?,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    explorationVersion: Int,
    private val oppiaLogger: OppiaLogger,
    private val analyticsController: AnalyticsController,
    private val loggingIdentifierController: LoggingIdentifierController
  ) {
    /**
     * The [StateAnalyticsLogger] corresponding to the current, pending state, or null if there is
     * none (i.e. the most recent state is completed, or the terminal state has been reached).
     */
    val stateAnalyticsLogger by lazy { mutableStateAnalyticsLogger.asStateFlow() }

    private val mutableStateAnalyticsLogger = MutableStateFlow<StateAnalyticsLogger?>(null)

    private val baseLogger by lazy {
      BaseLogger(oppiaLogger, analyticsController, profileId, installationId)
    }
    private val learnerDetailsContext by lazy {
      createLearnerDetailsContext(installationId, learnerId)
    }
    private val explorationLogContext by lazy {
      learnerDetailsContext?.let { learnerDetails ->
        val learnerSessionId = loggingIdentifierController.getSessionIdFlow().value
        ExplorationContext.newBuilder().apply {
          sessionId = learnerSessionId
          this.explorationId = explorationId
          this.classroomId = classroomId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationVersion = explorationVersion
          this.learnerDetails = learnerDetails
        }.build()
      }?.ensureNonEmpty()
    }

    /** Logs that the current exploration was started by resuming from previous progress. */
    fun logResumeExploration() {
      baseLogger.maybeLogLearnerEvent(learnerDetailsContext) {
        createAnalyticsEvent(it, EventBuilder::setResumeExplorationContext)
      }
    }

    /** Logs that the current exploration was restarted, ignoring previously available progress. */
    fun logStartExplorationOver() {
      baseLogger.maybeLogLearnerEvent(learnerDetailsContext) {
        createAnalyticsEvent(it, EventBuilder::setStartOverExplorationContext)
      }
    }

    /** Logs that an exploration has been started by the learner. */
    fun logStartExploration() {
      getExpectedStateLogger()?.logStartExploration()
    }

    /** Logs that the current exploration has been exited (i.e. not finished). */
    fun logExitExploration() {
      getExpectedStateLogger()?.logExitExploration()
    }

    /** Logs that the current exploration has been fully completed by the learner. */
    fun logFinishExploration() {
      getExpectedStateLogger()?.logFinishExploration()
    }

    /** Logs that the current exploration progress has been saved successfully. */
    fun logProgressSavingSuccess() {
      getExpectedStateLogger()?.logProgressSavingSuccess()
    }

    /** Logs that the current exploration progress has failed to save. */
    fun logProgressSavingFailure() {
      getExpectedStateLogger()?.logProgressSavingFailure()
    }

    /** Logs that the user has left the lesson advertently (attempted to save). */
    fun logLessonSavedAdvertently() {
      getExpectedStateLogger()?.logLessonSavedAdvertently()
    }

    /** Logs that correct answer was submitted in a resumed lesson. */
    fun logResumeLessonSubmitCorrectAnswer() {
      getExpectedStateLogger()?.logResumeLessonSubmitCorrectAnswer()
    }

    /** Logs that incorrect answer was submitted in a resumed lesson. */
    fun logResumeLessonSubmitIncorrectAnswer() {
      getExpectedStateLogger()?.logResumeLessonSubmitIncorrectAnswer()
    }

    /**
     * Begins analytics logging for the specified [newState], returning the [StateAnalyticsLogger]
     * that can be used to log events for the [State].
     *
     * This overrides the current [stateAnalyticsLogger].
     *
     * [endCard] should be called when the current [State] is completed.
     */
    fun startCard(newState: State): StateAnalyticsLogger {
      return StateAnalyticsLogger(
        loggingIdentifierController, baseLogger, newState, explorationLogContext
      ).also {
        if (!mutableStateAnalyticsLogger.compareAndSet(expect = null, update = it)) {
          oppiaLogger.w(
            "LearnerAnalyticsLogger", "Attempting to start a card without ending the previous."
          )

          // Force the logger to a new state.
          mutableStateAnalyticsLogger.value = it
        }
      }
    }

    /**
     * Resets the current [stateAnalyticsLogger], indicating that the most recent [State] has been
     * completed.
     */
    fun endCard() {
      if (mutableStateAnalyticsLogger.value == null) {
        oppiaLogger.w("LearnerAnalyticsLogger", "Attempting to end a card not yet started.")
      } else mutableStateAnalyticsLogger.value = null
    }

    private fun getExpectedStateLogger(): StateAnalyticsLogger? {
      return mutableStateAnalyticsLogger.value.also {
        if (it == null) {
          oppiaLogger.w("LearnerAnalyticsLogger", "Attempting to log a state event outside state.")
        }
      }
    }
  }

  /** Analytics logger for [State]-specific events. */
  class StateAnalyticsLogger internal constructor(
    private val loggingIdentifierController: LoggingIdentifierController,
    private val baseLogger: BaseLogger,
    private val currentState: State,
    private val baseExplorationLogContext: ExplorationContext?
  ) {
    private val linkedSkillId by lazy { currentState.linkedSkillId }

    /** Logs that an exploration has been started (at this state). */
    internal fun logStartExploration() {
      logStateEvent(EventBuilder::setStartExplorationContext)
    }

    /** Logs that the current exploration has been exited (at this state). */
    internal fun logExitExploration() {
      logStateEvent(EventBuilder::setExitExplorationContext)
    }

    /** Logs that the current exploration has been finished (at this state). */
    internal fun logFinishExploration() {
      logStateEvent(EventBuilder::setFinishExplorationContext)
    }

    /** Logs that the current exploration progress has been saved successfully. */
    internal fun logProgressSavingSuccess() {
      logStateEvent(EventBuilder::setProgressSavingSuccessContext)
    }

    /** Logs that the current exploration progress has failed to save. */
    internal fun logProgressSavingFailure() {
      logStateEvent(EventBuilder::setProgressSavingFailureContext)
    }

    /** Logs that the user has left the lesson advertently (attempted to save). */
    internal fun logLessonSavedAdvertently() {
      logStateEvent(EventBuilder::setLessonSavedAdvertentlyContext)
    }

    /** Logs that correct answer was submitted in a resumed lesson. */
    internal fun logResumeLessonSubmitCorrectAnswer() {
      logStateEvent(EventBuilder::setResumeLessonSubmitCorrectAnswerContext)
    }

    /** Logs that incorrect answer was submitted in a resumed lesson. */
    internal fun logResumeLessonSubmitIncorrectAnswer() {
      logStateEvent(EventBuilder::setResumeLessonSubmitIncorrectAnswerContext)
    }

    /** Logs that this card has been started. */
    fun logStartCard() {
      logStateEvent(linkedSkillId, ::createCardContext, EventBuilder::setStartCardContext)
    }

    /** Logs that this card has been completed. */
    fun logEndCard() {
      logStateEvent(linkedSkillId, ::createCardContext, EventBuilder::setEndCardContext)
    }

    /** Logs that the hint corresponding to [hintIndex] has been unlocked by the learner. */
    fun logHintUnlocked(hintIndex: Int) {
      logStateEvent(hintIndex, ::createHintContext, EventBuilder::setHintUnlockedContext)
    }

    /** Logs that the hint corresponding to [hintIndex] has been revealed by the learner. */
    fun logRevealHint(hintIndex: Int) {
      logStateEvent(hintIndex, ::createHintContext, EventBuilder::setRevealHintContext)
    }

    /**
     * Logs the event indicating that the learner has viewed a hint corresponding to [hintIndex],
     * excluding the first-time viewing.
     */
    fun logViewHint(hintIndex: Int) {
      logStateEvent(hintIndex, ::createHintContext, EventBuilder::setViewExistingHintContext)
    }

    /** Logs that the solution to the current card has been unlocked by the learner. */
    fun logSolutionUnlocked() {
      logStateEvent(EventBuilder::setSolutionUnlockedContext)
    }

    /** Logs that the solution to the current card has been viewed by the learner. */
    fun logRevealSolution() {
      logStateEvent(EventBuilder::setRevealSolutionContext)
    }

    /**
     * Logs the event indicating that the learner has viewed the solution to the current card,
     * excluding the first-time viewing.
     */
    fun logViewSolution() {
      logStateEvent(EventBuilder::setViewExistingSolutionContext)
    }

    /**
     * Logs that the learner submitted an answer, where [isCorrect] indicates whether the answer was
     * labelled as correct, and [userAnswer] was the actual structured answer submitted by the
     * learner (in the provided [interaction]).
     */
    fun logSubmitAnswer(interaction: Interaction, userAnswer: UserAnswer, isCorrect: Boolean) {
      val answer = userAnswer.answer
      val stringifiedUserAnswer = when (answer.objectTypeCase) {
        InteractionObject.ObjectTypeCase.NORMALIZED_STRING -> answer.normalizedString
        InteractionObject.ObjectTypeCase.SIGNED_INT -> answer.signedInt.toString()
        InteractionObject.ObjectTypeCase.REAL -> answer.real.toString()
        InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT -> answer.nonNegativeInt.toString()
        InteractionObject.ObjectTypeCase.FRACTION -> answer.fraction.toAnswerString()
        InteractionObject.ObjectTypeCase.CLICK_ON_IMAGE ->
          "(${answer.clickOnImage.clickPosition.x}, ${answer.clickOnImage.clickPosition.y})"
        InteractionObject.ObjectTypeCase.RATIO_EXPRESSION -> answer.ratioExpression.toAnswerString()
        InteractionObject.ObjectTypeCase.SET_OF_TRANSLATABLE_HTML_CONTENT_IDS -> {
          val choices = interaction.customizationArgsMap["choices"]
            ?.schemaObjectList
            ?.schemaObjectList
            ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml.contentId }
            ?.toSet()
            ?: setOf()
          val contentIds = answer.setOfTranslatableHtmlContentIds.contentIdsList
          contentIds.joinToString(prefix = "[", postfix = "]") {
            choices.indexOf(it.contentId).toString()
          }
        }
        InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS -> {
          val choices = interaction.customizationArgsMap["choices"]
            ?.schemaObjectList
            ?.schemaObjectList
            ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml.contentId }
            ?.toSet()
            ?: setOf()
          val contentIdLists = answer.listOfSetsOfTranslatableHtmlContentIds.contentIdListsList
          contentIdLists.joinToString(prefix = "[", postfix = "]") { contentIdsList ->
            contentIdsList.contentIdsList.joinToString(prefix = "[", postfix = "]") {
              choices.indexOf(it.contentId).toString()
            }
          }
        }
        InteractionObject.ObjectTypeCase.MATH_EXPRESSION -> answer.mathExpression
        InteractionObject.ObjectTypeCase.BOOL_VALUE,
        InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
        InteractionObject.ObjectTypeCase.TRANSLATABLE_SET_OF_NORMALIZED_STRING,
        InteractionObject.ObjectTypeCase.TRANSLATABLE_HTML_CONTENT_ID,
        InteractionObject.ObjectTypeCase.SET_OF_HTML_STRING,
        InteractionObject.ObjectTypeCase.IMAGE_WITH_REGIONS,
        InteractionObject.ObjectTypeCase.NUMBER_WITH_UNITS,
        InteractionObject.ObjectTypeCase.OBJECTTYPE_NOT_SET, null -> null
      }

      logStateEvent(
        stringifiedUserAnswer,
        isCorrect,
        ::createSubmitAnswerContext,
        EventBuilder::setSubmitAnswerContext
      )
    }

    /**
     * Logs that the learner started playing a voice over audio track corresponding to [contentId]
     * with language code [languageCode] (or null if something failed when retrieving the content ID
     * or language code--note that this may affect whether the event is logged).
     */
    fun logPlayVoiceOver(contentId: String?, languageCode: String?) {
      logStateEvent(
        contentId,
        languageCode,
        ::createVoiceoverActionContext,
        EventBuilder::setPlayVoiceOverContext
      )
    }

    /**
     * Logs that the learner stopped playing a voice over audio track corresponding to [contentId]
     * with language code [languageCode] (see [logPlayVoiceOver] for caveats for both [contentId]
     * and [languageCode]).
     */
    fun logPauseVoiceOver(contentId: String?, languageCode: String?) {
      logStateEvent(
        contentId,
        languageCode,
        ::createVoiceoverActionContext,
        EventBuilder::setPauseVoiceOverContext
      )
    }

    /**
     * Logs that the learner has demonstrated an invested engagement in the lesson (that is, they've
     * played far enough in the lesson to indicate that they're not just quickly browsing & then
     * leaving).
     */
    fun logInvestedEngagement() {
      logStateEvent(EventBuilder::setReachInvestedEngagement)
    }

    /**
     * Logs that the learner has switched an ongoing lesson's content language from the language
     * indicated by [fromLanguage] to the language indicated by [toLanguage].
     */
    fun logSwitchInLessonLanguage(fromLanguage: OppiaLanguage, toLanguage: OppiaLanguage) {
      logStateEvent(
        fromLanguage,
        toLanguage,
        ::createSwitchInLessonLanguageContext,
        EventBuilder::setSwitchInLessonLanguage
      )
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

    private fun <D1, D2, T> logStateEvent(
      detail1: D1,
      detail2: D2,
      baseContextFactory: (D1, D2, ExplorationContext) -> T,
      setter: EventBuilder.(T) -> EventBuilder
    ) {
      baseLogger.maybeLogEvent(
        computeLogContext()?.let {
          createAnalyticsEvent(baseContextFactory(detail1, detail2, it), setter)
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

  /**
   * The common base logger used by [ExplorationAnalyticsLogger] and [StateAnalyticsLogger], and
   * should never be interacted with outside this class.
   */
  internal class BaseLogger internal constructor(
    private val oppiaLogger: OppiaLogger,
    private val analyticsController: AnalyticsController,
    private val profileId: ProfileId,
    private val installationId: String?
  ) {
    /**
     * Logs a learner-specific event defined by [learnerDetailsContext] and converted to a full
     * [EventContext] by the provided [creationDelegate].
     *
     * See [maybeLogEvent] for specifics on when the event is logged.
     *
     * Also, this method is only meant to be used as a convenience function for nicer syntax when
     * logging certain learner events.
     */
    fun maybeLogLearnerEvent(
      learnerDetailsContext: LearnerDetailsContext?,
      creationDelegate: (LearnerDetailsContext) -> EventContext
    ) {
      // Note that this tries to ensure that the event is always logged even if details are missing.
      maybeLogEvent(
        creationDelegate(learnerDetailsContext ?: LearnerDetailsContext.getDefaultInstance())
      )
    }

    /** See [AnalyticsController.maybeLogEvent]. */
    fun maybeLogEvent(context: EventContext?) =
      analyticsController.maybeLogEvent(installationId, context, profileId, oppiaLogger)

    internal companion object {
      /**
       * Conditionally logs the event specified by [context] for the provided [installId] in this
       * logger if [context] is not null, otherwise logs an error analytics event for error tracking
       * in the analytics pipeline.
       */
      internal fun AnalyticsController.maybeLogEvent(
        installId: String?,
        context: EventContext?,
        profileId: ProfileId?,
        oppiaLogger: OppiaLogger
      ) {
        if (context != null) {
          logImportantEvent(context, profileId)
        } else {
          oppiaLogger.e(
            "LearnerAnalyticsLogger",
            "Event is being dropped due to incomplete event (or missing learner ID for profile)."
          )
          logImportantEvent(createFailedToLogLearnerAnalyticsEvent(installId), profileId)
        }
      }
    }
  }

  private companion object {
    private const val DEFAULT_INSTALLATION_ID = "unknown_installation_id"

    private fun <T : MessageLite> T.ensureNonEmpty(): T? =
      takeIf { it != it.defaultInstanceForType }

    private fun createLearnerDetailsContext(
      installationId: String?,
      learnerId: String?
    ): LearnerDetailsContext? {
      return createLearnerDetailsContextWithIdsPresent(
        installationId?.takeUnless(String::isEmpty), learnerId?.takeUnless(String::isEmpty)
      ).ensureNonEmpty()
    }

    private fun createLearnerDetailsContextWithIdsPresent(
      installationId: String?,
      learnerId: String?
    ): LearnerDetailsContext {
      return LearnerDetailsContext.newBuilder().apply {
        installationId?.let { installId = it }
        learnerId?.let { this.learnerId = it }
      }.build()
    }

    private fun createCardContext(
      skillId: String,
      explorationDetails: ExplorationContext
    ) = CardContext.newBuilder().apply {
      this.skillId = skillId
      this.explorationDetails = explorationDetails
    }.build()

    private fun createHintContext(
      hintIndex: Int,
      explorationDetails: ExplorationContext
    ) = HintContext.newBuilder().apply {
      this.hintIndex = hintIndex
      this.explorationDetails = explorationDetails
    }.build()

    private fun createSubmitAnswerContext(
      stringifiedAnswer: String?,
      isAnswerCorrect: Boolean,
      explorationDetails: ExplorationContext
    ) = SubmitAnswerContext.newBuilder().apply {
      stringifiedAnswer?.let { this.stringifiedAnswer = it }
      this.isAnswerCorrect = isAnswerCorrect
      this.explorationDetails = explorationDetails
    }.build()

    private fun createVoiceoverActionContext(
      contentId: String?,
      languageCode: String?,
      explorationDetails: ExplorationContext
    ) = VoiceoverActionContext.newBuilder().apply {
      contentId?.let { this.contentId = it }
      languageCode?.let { this.languageCode = languageCode }
      this.explorationDetails = explorationDetails
    }.build()

    private fun createSwitchInLessonLanguageContext(
      fromLanguage: OppiaLanguage,
      toLanguage: OppiaLanguage,
      explorationDetails: ExplorationContext
    ) = EventLog.SwitchInLessonLanguageEventContext.newBuilder().apply {
      switchFromLanguage = fromLanguage
      switchToLanguage = toLanguage
      this.explorationDetails = explorationDetails
    }.build()

    private fun <T> createAnalyticsEvent(
      baseContext: T,
      setter: EventBuilder.(T) -> EventContext.Builder
    ) = EventContext.newBuilder().setter(baseContext).build()

    private fun createFailedToLogLearnerAnalyticsEvent(installId: String?): EventContext {
      return EventContext.newBuilder().apply {
        installIdForFailedAnalyticsLog = installId ?: DEFAULT_INSTALLATION_ID
      }.build()
    }
  }
}
