package org.oppia.android.app.view

import dagger.Binds
import dagger.Module

/** Module for providing [ViewComponent.Builder]. */
@Module
interface ViewComponentBuilderModule {
  @Binds
  fun bindViewComponentBuilder(impl: ViewComponentImpl.Builder): ViewComponent.Builder
}
