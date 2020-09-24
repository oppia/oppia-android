package org.oppia.domain.oppialogger

/** Listener that gets created at application startup. */
interface ApplicationStartupListener {

  /** Gets called at application creation. */
  fun onCreate()
}
