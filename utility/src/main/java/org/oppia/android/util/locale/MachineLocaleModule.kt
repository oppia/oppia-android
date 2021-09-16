package org.oppia.android.util.locale

import dagger.Binds
import dagger.Module

/** Module for providing production implementations of [OppiaLocale.MachineLocale]. */
@Module
interface MachineLocaleModule {
  @Binds
  fun bindMachineLocale(impl: MachineLocaleImpl): OppiaLocale.MachineLocale
}
