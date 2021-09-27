package org.oppia.android.app.fragment

import javax.inject.Provider

/** Activity-level injector for retrieving instances of [FragmentComponent.Builder]. */
interface FragmentComponentBuilderInjector {
  /** Returns a Dagger [Provider] for [FragmentComponent.Builder]. */
  fun getFragmentComponentBuilderProvider(): Provider<FragmentComponent.Builder>
}
