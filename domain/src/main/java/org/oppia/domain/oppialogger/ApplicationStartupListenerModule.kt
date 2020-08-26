package org.oppia.domain.oppialogger

import dagger.Module
import dagger.multibindings.Multibinds

/** Binds multiple dependencies that implement [ApplicationStartupListener] into a set. */
@Module
interface ApplicationStartupListenerModule {

  @Multibinds
  fun bindStartupListenerSet(): Set<ApplicationStartupListener>
}
