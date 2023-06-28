package org.oppia.android.domain.exploration

import org.oppia.android.app.model.ProfileId

/**
 * Private class that encapsulates the mutable state of an [ExplorationActiveTimeController].
 * This class is not thread-safe, so owning classes should ensure synchronized access.
 */
internal class TimerSessionState {

  /** The timestamp in millis when the timer was started. */
  internal var sessionStartTime: Long = 0L

  /** The profileId of the profile currently logged in. */
  internal lateinit var currentProfileId: ProfileId

  /** The id of the topic that the current exploration belongs to. */
  internal lateinit var currentTopicId: String

  /** Indicates whether the app is in the foreground. */
  internal var isAppInForeground: Boolean = false

  /** Indicates whether an exploration session is active. */
  internal var isExplorationStarted: Boolean = false
}
