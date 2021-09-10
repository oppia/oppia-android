package org.oppia.android.util.locale

import dagger.Binds
import dagger.Module

@Module
interface MachineLocaleModule {
  @Binds
  fun bindMachineLocale(impl: MachineLocaleImpl): OppiaLocale.MachineLocale
}
