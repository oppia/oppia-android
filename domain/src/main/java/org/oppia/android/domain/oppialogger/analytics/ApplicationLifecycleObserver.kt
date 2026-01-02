package org.oppia.android.domain.oppialogger.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Observer that observes application and activity lifecycle. */
@Singleton
@OptIn(ObsoleteCoroutinesApi::class)
class ApplicationLifecycleObserver @Inject constructor(
  private val application: Application,
  private val oppiaClock: OppiaClock,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val applicationLifecycleLoggerProvider: Provider<ApplicationLifecycleLogger>,
  private val listenersProvider: Provider<Set<@JvmSuppressWildcards ApplicationLifecycleListener>>
) : ApplicationStartupListener, LifecycleObserver, Application.ActivityLifecycleCallbacks {
  private lateinit var commandQueue: SendChannel<LifecycleChangeMessage>

  override fun onCreateStarted() {
    // Create the command queue so that lifecycle messages can be recorded without loss, then start
    // listening for them. Messages cannot be processed immediately since logging requirements and
    // other listener callbacks may require dependencies not yet available this early in the app
    // lifecycle.
    commandQueue = createObserverCommandActor(appStartupTimeMillis = oppiaClock.getCurrentTimeMs())
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    application.registerActivityLifecycleCallbacks(this)
  }

  override fun onCompletedInitialization() {
    // The app is fully ready to go so start enable full logging and process any previously missed
    // commands.
    enqueueMessage(LifecycleChangeMessage::Initialize)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    enqueueMessage(LifecycleChangeMessage::AppInForeground)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    enqueueMessage(LifecycleChangeMessage::AppInBackground)
  }

  override fun onActivityResumed(activity: Activity) {
    val currentScreen = activity.intent.extractCurrentAppScreenName()
    enqueueMessage { time -> LifecycleChangeMessage.ActivityResumed(time, currentScreen) }
  }

  override fun onActivityPaused(activity: Activity) {
    enqueueMessage(LifecycleChangeMessage::ActivityPaused)
  }

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}

  private fun createObserverCommandActor(
    appStartupTimeMillis: Long
  ): SendChannel<LifecycleChangeMessage> {
    lateinit var state: ObserverState
    var replayBuffer: MutableSet<LifecycleChangeMessage>? = mutableSetOf()
    return CoroutineScope(backgroundCoroutineDispatcher).actor(capacity = Channel.UNLIMITED) {
      for (message in channel) {
        // First check this is a case of a post-initialization message coming in before
        // initialization. That's supported by this command queue since it will store them in a
        // replay buffer.
        val buffer = replayBuffer
        when {
          message is LifecycleChangeMessage.Initialize -> {
            checkNotNull(buffer) { "Attempting to re-initialize command queue." }

            // It's now safe to fetch all dependencies for processing.
            state = ObserverState(
              appStartupTimeMillis,
              applicationLifecycleLoggerProvider.get(),
              listenersProvider.get()
            )

            // Process any other messages that came in.
            state.processMessage(message)
            for (it in buffer) {
              check(it !is LifecycleChangeMessage.Initialize) {
                "Attempting to re-initialize command queue."
              }
              state.processMessage(it)
            }
            buffer.clear()
            replayBuffer = null
          }
          buffer != null -> buffer += message // Queue the message for later.
          else -> state.processMessage(message) // Otherwise it can be processed immediately.
        }
      }
    }
  }

  private fun enqueueMessage(factory: (Long) -> LifecycleChangeMessage) {
    // Failures to enqueue lifecycle changes could be catastrophic to internal app state so it's
    // almost certainly better to crash than try to recover. It's also expected that such a failure
    // should be impossible since the queue is configured to be unlimited.
    check(commandQueue.trySend(factory(oppiaClock.getCurrentTimeMs())).isSuccess) {
      "Failed to enqueue command to capture lifecycle change."
    }
  }

  private sealed class LifecycleChangeMessage {
    abstract val timestamp: Long

    data class Initialize(override val timestamp: Long) : LifecycleChangeMessage()
    data class AppInForeground(override val timestamp: Long) : LifecycleChangeMessage()
    data class AppInBackground(override val timestamp: Long) : LifecycleChangeMessage()
    data class ActivityResumed(
      override val timestamp: Long,
      val activityScreen: ScreenName
    ) : LifecycleChangeMessage()
    data class ActivityPaused(override val timestamp: Long) : LifecycleChangeMessage()
  }

  private class ObserverState(
    val appStartupTimeMillis: Long,
    val applicationLifecycleLogger: ApplicationLifecycleLogger,
    val applicationLifecycleListeners: Set<ApplicationLifecycleListener>
  ) {
    fun processMessage(message: LifecycleChangeMessage) {
      when (message) {
        is LifecycleChangeMessage.Initialize ->
          applicationLifecycleLogger.recordAppOpened(appStartupTimeMillis)
        is LifecycleChangeMessage.AppInForeground -> {
          applicationLifecycleListeners.forEach(ApplicationLifecycleListener::onAppInForeground)
          applicationLifecycleLogger.recordAppInForeground(message.timestamp)
        }
        is LifecycleChangeMessage.AppInBackground -> {
          applicationLifecycleListeners.forEach(ApplicationLifecycleListener::onAppInBackground)
          applicationLifecycleLogger.recordAppInBackground(message.timestamp)
        }
        is LifecycleChangeMessage.ActivityResumed -> {
          applicationLifecycleLogger.recordActivityResumed(
            message.activityScreen, appStartupTimeMillis, message.timestamp
          )
        }
        is LifecycleChangeMessage.ActivityPaused ->
          applicationLifecycleLogger.recordActivityPaused()
      }
    }
  }
}
