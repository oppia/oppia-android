package org.oppia.app.shim

import dagger.Binds
import dagger.Module

@Module
interface ViewBindingShimModule {

  @Binds
  fun provideViewBindingShim(viewBindingShim: ViewBindingShimImpl): ViewBindingShim
}
