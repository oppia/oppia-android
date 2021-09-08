package org.oppia.android.app.view

import android.view.View

interface ViewComponentFactory {
  /** Returns a new [ViewComponent] for the specified view. */
  fun createViewComponent(view: View): ViewComponent
}
