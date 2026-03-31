package org.oppia.android.testing.system

import dagger.Binds
import dagger.Module
import org.oppia.android.util.system.TerminalController

/** Dagger module for [FakeTerminalController]. */
@Module
interface FakeTerminalControllerModule {
  @Binds
  fun bindTerminalController(impl: FakeTerminalController): TerminalController
}
