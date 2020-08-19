package org.oppia.domain.oppialogger

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Module
class OppiaWorkManagerModule {
  @Provides
  fun providesOppiaWorkManager(oppiaWorkManager: OppiaWorkManager): OppiaWorkManager {
    return oppiaWorkManager
  }
}
