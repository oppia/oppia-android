package org.oppia.android.util.system

/** Application-level injector for system-related utilities. */
interface OppiaClockInjector {
  /** Returns an [OppiaClock] from the Dagger graph. */
  fun getOppiaClock(): OppiaClock
}
