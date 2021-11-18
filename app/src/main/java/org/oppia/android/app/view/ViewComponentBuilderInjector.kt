package org.oppia.android.app.view

import javax.inject.Provider

/** Fragment-level injector for retrieving instances of [ViewComponent.Builder]. */
interface ViewComponentBuilderInjector {
  /** Returns a Dagger [Provider] for [ViewComponent.Builder]. */
  fun getViewComponentBuilderProvider(): Provider<ViewComponent.Builder>
}
