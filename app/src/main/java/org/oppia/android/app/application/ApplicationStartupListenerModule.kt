package org.oppia.android.app.application

import dagger.Module
import dagger.multibindings.Multibinds
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Binds multiple dependencies that implement [ApplicationStartupListener] into a set. */
@Module
interface ApplicationStartupListenerModule {

  @Multibinds
  fun bindStartupListenerSet(): Set<ApplicationStartupListener>
}
