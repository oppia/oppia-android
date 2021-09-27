package org.oppia.android.util.locale.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.locale.MachineLocaleImpl
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale

/** Module for providing testing implementations of locale utilities. */
@Module
interface LocaleTestModule {
  @Binds
  fun bindMachineLocale(impl: MachineLocaleImpl): OppiaLocale.MachineLocale

  @Binds
  fun bindBidiFormatterFactory(impl: TestOppiaBidiFormatter.FactoryImpl): OppiaBidiFormatter.Factory
}
