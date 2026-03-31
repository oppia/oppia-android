package org.oppia.android.util.system

import dagger.Binds
import dagger.Module

/** Dagger module for [TerminalController]. */
@Module
interface TerminalControllerModule {
  @Binds
  fun bindTerminalController(impl: TerminalControllerImpl): TerminalController
}
