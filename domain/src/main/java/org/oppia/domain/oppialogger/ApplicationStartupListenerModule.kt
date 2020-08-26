package org.oppia.domain.oppialogger

import dagger.Module
import dagger.multibindings.Multibinds

@Module
interface ApplicationStartupListenerModule {

  @Multibinds
  fun bindStartupListenerSet(): Set<ApplicationStartupListener>
}
