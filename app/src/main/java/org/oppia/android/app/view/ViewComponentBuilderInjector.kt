package org.oppia.android.app.view

import javax.inject.Provider

interface ViewComponentBuilderInjector {
  fun getViewComponentBuilderProvider(): Provider<ViewComponent.Builder>
}
