package org.oppia.app

import dagger.Binds
import dagger.Module

@Module
interface IntentFactoryShimModule {

  @Binds
  fun provideIntentFactoryShim(intentFactoryShim: IntentFactoryShim): IntentFactoryShimInterface
}