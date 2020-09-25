package org.oppia.android.app.shim

import dagger.Binds
import dagger.Module

@Module
interface IntentFactoryShimModule {

  @Binds
  fun provideIntentFactoryShim(intentFactoryShim: IntentFactoryShimImpl): IntentFactoryShim
}
