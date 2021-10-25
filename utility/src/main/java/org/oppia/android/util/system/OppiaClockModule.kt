package org.oppia.android.util.system

import dagger.Binds
import dagger.Module

/** Module for binding [OppiaClock] to an implementation that relies on real time. */
@Module
interface OppiaClockModule {
  @Binds
  fun bindOppiaClock(impl: OppiaClockImpl): OppiaClock
}
