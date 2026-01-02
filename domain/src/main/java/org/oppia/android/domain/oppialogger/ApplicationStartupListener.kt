package org.oppia.android.domain.oppialogger

/**
 * Monitors the creation lifecycle of the app.
 *
 * NOTE TO DEVELOPERS: Implementations of this listener run *very* early in the app lifecycle, even
 * before platform parameters have been fully initialized. Extreme care must be taken to ensure that
 * all injections are either injected as `Provider`s or do not transitively depend on platform
 * parameters. Further restrictions about these types of dependencies are covered in the method
 * documentation below.
 *
 * Also, note that the two lifecycle callback methods below are deliberately called on distinct
 * thread types (main/background) so care must be taken in implementations to account for this. The
 * two methods will never race each other, and are both guaranteed to be called in order and exactly
 * once for the lifetime of the application.
 */
interface ApplicationStartupListener {

  /**
   * Called immediately upon application start.
   *
   * This function must not interact with any other components that may require state initialization
   * in the app, including analytics, `WorkManager`, and especially platform parameters.
   * Implementations can use this to perform critical startup self state initialization. If any
   * persistence or broad system interaction is needed during startup then that should happen in
   * [onCompletedInitialization].
   *
   * Note: This is guaranteed to always be called before any other entrypoint logic executes
   * including `SplashActivity`, direct activity recreation, and waking up the app to start a
   * background worker.
   *
   * Important: This will be called on the main thread and should never block.
   */
  fun onCreateStarted()

  /**
   * Called when the application has been initialized sufficiently well to now be safe for broad
   * system interaction.
   *
   * Most critically this will be called after platform parameters have fully initialized making
   * broad app system interaction safe.
   *
   * Note: This may get called after other entry points are called (such as `SplashActivity`, direct
   * activity creation, and background worker interaction), so care needs to be taken if this
   * listener influences entry point state (it's highly recommended to avoid that if possible).
   *
   * Most startup initialization logic is expected to go in the implementation of this function to
   * help ensure general startup safety.
   *
   * Important: This will be called on a background thread, NOT the main thread.
   */
  fun onCompletedInitialization()
}
