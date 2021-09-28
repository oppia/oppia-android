package org.oppia.android.util.locale

import dagger.Binds
import dagger.Module

/** Module for providing production implementations of locale utilities. */
@Module
interface LocaleProdModule {
  @Binds
  fun bindMachineLocale(impl: MachineLocaleImpl): OppiaLocale.MachineLocale

  @Binds
  fun bindBidiFormatterFactory(impl: OppiaBidiFormatterImpl.FactoryImpl): OppiaBidiFormatter.Factory
}
