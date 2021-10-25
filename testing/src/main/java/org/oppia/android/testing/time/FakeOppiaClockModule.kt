package org.oppia.android.testing.time

import dagger.Binds
import dagger.Module
import org.oppia.android.util.system.OppiaClock

/** Module for providing [FakeOppiaClock]. */
@Module
interface FakeOppiaClockModule {
  @Binds
  fun bindFakeOppiaClock(fakeOppiaClock: FakeOppiaClock): OppiaClock
}
