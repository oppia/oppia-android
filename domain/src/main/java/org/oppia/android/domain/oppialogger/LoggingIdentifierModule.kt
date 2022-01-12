package org.oppia.android.domain.oppialogger

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.system.RealUUIDImpl
import org.oppia.android.util.system.UUIDWrapper

@Qualifier
annotation class DeviceIdSeed

/** Provider to return any constants required during operations on logging identifiers. */
@Module
class LoggingIdentifierModule {

  @Provides
  @DeviceIdSeed
  fun provideDeviceIdSeed(oppiaClock: OppiaClock): Long = oppiaClock.getCurrentTimeMs()

  @Provides
  fun provideUUIDWrapper(): UUIDWrapper = RealUUIDImpl()
}
