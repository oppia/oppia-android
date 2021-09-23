package org.oppia.android.app.view

import android.view.View

/** Factory for creating new [ViewComponent]s. */
interface ViewComponentFactory {
  /** Returns a new [ViewComponent] for the specified view. */
  fun createViewComponent(view: View): ViewComponent
}
