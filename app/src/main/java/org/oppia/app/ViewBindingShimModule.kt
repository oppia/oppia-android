package org.oppia.app

import dagger.Binds
import dagger.Module

@Module
interface ViewBindingShimModule {

  @Binds
  fun provideViewBindingShim(viewBindingShim: ViewBindingShim): ViewBindingShimInterface
}