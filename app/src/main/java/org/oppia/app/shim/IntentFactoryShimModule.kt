package org.oppia.app.shim

import dagger.Binds
import dagger.Module

@Module
interface IntentFactoryShimModule {

  @Binds
  fun provideIntentFactoryShim(intentFactoryShim: IntentFactoryShim): IntentFactoryShimInterface
}
