package org.oppia.android.domain.oppialogger

import dagger.Module
import dagger.Provides
import org.oppia.android.util.system.OppiaClock

/** Provider to return any constants required during operations on logging identifiers. */
@Module
class LoggingIdentifierModule {
  @Provides
  @ApplicationIdSeed
  fun provideApplicationIdSeed(oppiaClock: OppiaClock): Long = oppiaClock.getCurrentTimeMs()
}
