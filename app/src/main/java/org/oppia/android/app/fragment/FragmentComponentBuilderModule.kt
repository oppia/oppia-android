package org.oppia.android.app.fragment

import dagger.Binds
import dagger.Module

/** Module for providing [FragmentComponentBuilderInjector]. */
@Module
interface FragmentComponentBuilderModule {
  @Binds
  fun bindFragmentComponentBuilder(impl: FragmentComponentImpl.Builder): FragmentComponent.Builder
}
