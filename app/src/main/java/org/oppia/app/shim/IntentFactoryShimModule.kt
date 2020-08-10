package org.oppia.app.shim

import dagger.Binds
import dagger.Module
import org.oppia.app.shim.IntentFactoryShim
import org.oppia.app.shim.IntentFactoryShimInterface

@Module
interface IntentFactoryShimModule {

  @Binds
  fun provideIntentFactoryShim(intentFactoryShim: IntentFactoryShim): IntentFactoryShimInterface
}