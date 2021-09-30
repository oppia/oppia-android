package org.oppia.android.util.system

/** Provider for [OppiaClockInjector]. */
interface OppiaClockInjectorProvider {
  /** Returns an [OppiaClockInjector]. */
  fun getOppiaClockInjector(): OppiaClockInjector
}
