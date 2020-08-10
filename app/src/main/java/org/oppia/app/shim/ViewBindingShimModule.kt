package org.oppia.app.shim

import dagger.Binds
import dagger.Module
import org.oppia.app.shim.ViewBindingShim
import org.oppia.app.shim.ViewBindingShimInterface

@Module
interface ViewBindingShimModule {

  @Binds
  fun provideViewBindingShim(viewBindingShim: ViewBindingShim): ViewBindingShimInterface
}