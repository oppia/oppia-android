package org.oppia.android.domain.oppialogger

import javax.inject.Inject
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.EventAction
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue

/** Logger that handles event logging. */
class OppiaLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  private val consoleLogger: ConsoleLogger,
  @LearnerStudyAnalytics private val learnerStudyAnalytics: PlatformParameterValue<Boolean>
) {
  /** Logs transition events. See [AnalyticsController.logTransitionEvent] for more context. */
  fun logTransitionEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logTransitionEvent(timestamp, eventAction, eventContext)
  }

  /** Logs transition events which are specifically related to Learner Study Analytics. These events
   * will only get logged if the value of [LearnerStudyAnalytics] platform parameter is set to true.
   * See [AnalyticsController.logTransitionEvent] for more context.
   */
  fun logLearnerAnalyticsTransitionEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    if (learnerStudyAnalytics.value) {
      analyticsController.logTransitionEvent(timestamp, eventAction, eventContext)
    }
  }

  /** Logs click events. See [AnalyticsController.logClickEvent] for more context. */
  fun logClickEvent(
    timestamp: Long,
    eventAction: EventAction,
    eventContext: EventLog.Context?
  ) {
    analyticsController.logClickEvent(timestamp, eventAction, eventContext)
  }

  /** Logs a verbose message with the specified tag. See [ConsoleLogger.v] for more context */
  fun v(tag: String, msg: String) {
    consoleLogger.v(tag, msg)
  }

  /**
   * Logs a verbose message with the specified tag, message and exception. See [ConsoleLogger.v]
   * for more context
   */
  fun v(tag: String, msg: String, tr: Throwable) {
    consoleLogger.v(tag, msg, tr)
  }

  /** Logs a debug message with the specified tag. See [ConsoleLogger.d] for more context */
  fun d(tag: String, msg: String) {
    consoleLogger.d(tag, msg)
  }

  /**
   * Logs a debug message with the specified tag, message and exception. See [ConsoleLogger.d] for
   * more context
   */
  fun d(tag: String, msg: String, tr: Throwable) {
    consoleLogger.d(tag, msg, tr)
  }

  /** Logs an info message with the specified tag. See [ConsoleLogger.i] for more context */
  fun i(tag: String, msg: String) {
    consoleLogger.i(tag, msg)
  }

  /**
   * Logs an info message with the specified tag, message and exception. See [ConsoleLogger.i] for
   * more context
   */
  fun i(tag: String, msg: String, tr: Throwable) {
    consoleLogger.i(tag, msg, tr)
  }

  /** Logs a warn message with the specified tag. See [ConsoleLogger.w] for more context */
  fun w(tag: String, msg: String) {
    consoleLogger.w(tag, msg)
  }

  /**
   * Logs a warn message with the specified tag, message and exception. See [ConsoleLogger.w] for
   * more context
   */
  fun w(tag: String, msg: String, tr: Throwable) {
    consoleLogger.w(tag, msg, tr)
  }

  /** Logs an error message with the specified tag. See [ConsoleLogger.e] for more context */
  fun e(tag: String, msg: String) {
    consoleLogger.e(tag, msg)
  }

  /**
   * Logs an error message with the specified tag, message and exception. See [ConsoleLogger.e] for
   * more context
   */
  fun e(tag: String, msg: String, tr: Throwable?) {
    consoleLogger.e(tag, msg, tr)
  }
  
  /**
   * Returns a generic data object that contains [deviceId] and [learnerId]. These identifiers are
   * logged across all Learner Study Analytics events.
   *
   * @param deviceId: device-specific identifier which is unique to each device.
   * @param learnerId: profile-specific identifier which is unique to each profile on a device.
   * */
  fun createGenericData(
    deviceId: String,
    learnerId: String
  ): EventLog.GenericData {
    return EventLog.GenericData.newBuilder()
      .setDeviceId(deviceId)
      .setLearnerId(learnerId)
      .build()
  }

  /**
   * Returns an exploration-specific data object that contains [sessionId], [explorationId],
   * [explorationVersion] and [stateName].
   *
   * @param sessionId: session-specific identifier which is unique to each session.
   * @param explorationId: id of the exploration.
   * @param explorationVersion: version of the exploration.
   * @param stateName: name of the current state.
   */
  fun createExplorationData(
    sessionId: String,
    explorationId: String,
    explorationVersion: String,
    stateName: String
  ): EventLog.ExplorationData {
    return EventLog.ExplorationData.newBuilder()
      .setSessionId(sessionId)
      .setExplorationId(explorationId)
      .setExplorationVersion(explorationVersion)
      .setStateName(stateName)
      .build()
  }

  /** Returns the context of an event related to exploration. */
  fun createExplorationContext(
    topicId: String,
    storyId: String,
    explorationId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setExplorationContext(
        EventLog.ExplorationContext.newBuilder()
          .setTopicId(topicId)
          .setStoryId(storyId)
          .setExplorationId(explorationId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to question. */
  fun createQuestionContext(
    questionId: String,
    skillId: List<String>
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setQuestionContext(
        EventLog.QuestionContext.newBuilder()
          .setQuestionId(questionId)
          .addAllSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to topic. */
  fun createTopicContext(
    topicId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setTopicContext(
        EventLog.TopicContext.newBuilder()
          .setTopicId(topicId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to story. */
  fun createStoryContext(
    topicId: String,
    storyId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setStoryContext(
        EventLog.StoryContext.newBuilder()
          .setTopicId(topicId)
          .setStoryId(storyId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to concept card. */
  fun createConceptCardContext(
    skillId: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setConceptCardContext(
        EventLog.ConceptCardContext.newBuilder()
          .setSkillId(skillId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to revision card. */
  fun createRevisionCardContext(
    topicId: String,
    subTopicId: Int
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setRevisionCardContext(
        EventLog.RevisionCardContext.newBuilder()
          .setTopicId(topicId)
          .setSubTopicId(subTopicId)
          .build()
      )
      .build()
  }

  /** Returns the context of an event related to starting an exploration card. */
  fun createStartCardContext(
    skillId: String, 
    genericData: EventLog.GenericData, 
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setStartCardContext(
        EventLog.StartCardContext.newBuilder()
          .setSkillId(skillId)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to ending an exploration card. */
  fun createEndCardContext(
    skillId: String,
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setEndCardContext(
        EventLog.EndCardContext.newBuilder()
          .setSkillId(skillId)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to offering a hint when it becomes available. */
  fun createHintOfferedContext(
    hintIndex: String,
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setHintOfferedContext(
        EventLog.HintOfferedContext.newBuilder()
          .setHintIndex(hintIndex)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to accessing a hint. */
  fun createAccessHintContext(
    hintIndex: String,
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAccessHintContext(
        EventLog.AccessHintContext.newBuilder()
          .setHintIndex(hintIndex)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to offering a solution when it becomes available. */
  fun createSolutionOfferedContext(
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setSolutionOfferedContext(
        EventLog.SolutionOfferedContext.newBuilder()
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to accessing a solution. */
  fun createAccessSolutionContext(
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAccessSolutionContext(
        EventLog.AccessSolutionContext.newBuilder()
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to submitting an answer. */
  fun createSubmitAnswerContext(
    isAnswerCorrect: Boolean,
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setSubmitAnswerContext(
        EventLog.SubmitAnswerContext.newBuilder()
          .setIsAnswerCorrect(isAnswerCorrect)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to playing a voice over. */
  fun createPlayVoiceOverContext(
    contentId: String,
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setPlayVoiceOverContext(
        EventLog.PlayVoiceOverContext.newBuilder()
          .setContentId(contentId)
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to backgrounding of the application. */
  fun createAppInBackgroundContext(
    genericData: EventLog.GenericData,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAppInBackgroundContext(
        EventLog.AppInBackgroundContext.newBuilder()
          .setGenericData(genericData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to foregrounding of the application. */
  fun createAppInForegroundContext(
    genericData: EventLog.GenericData,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAppInForegroundContext(
        EventLog.AppInForegroundContext.newBuilder()
          .setGenericData(genericData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to exiting an exploration. */
  fun createExitExplorationContext(
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setExitExplorationContext(
        EventLog.ExitExplorationContext.newBuilder()
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to finishing an exploration. */
  fun createFinishExplorationContext(
    genericData: EventLog.GenericData,
    explorationData: EventLog.ExplorationData
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setFinishExplorationContext(
        EventLog.FinishExplorationContext.newBuilder()
          .setGenericData(genericData)
          .setExplorationData(explorationData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to resuming an exploration. */
  fun createResumeExplorationContext(
    genericData: EventLog.GenericData,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setResumeExplorationContext(
        EventLog.ResumeExplorationContext.newBuilder()
          .setGenericData(genericData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to starting over an exploration. */
  fun createStartOverExplorationContext(
    genericData: EventLog.GenericData,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setStartOverExplorationContext(
        EventLog.StartOverExplorationContext.newBuilder()
          .setGenericData(genericData)
          .build()
      ).build()
  }

  /** Returns the context of an event related to deleting a profile. */
  fun createDeleteProfileContext(
    genericData: EventLog.GenericData,
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setDeleteProfileContext(
        EventLog.DeleteProfileContext.newBuilder()
          .setGenericData(genericData)
          .build()
      ).build()
  }
}
