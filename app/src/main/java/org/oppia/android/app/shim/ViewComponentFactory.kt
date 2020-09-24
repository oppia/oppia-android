package org.oppia.android.app.shim

import android.view.View
import org.oppia.android.app.view.ViewComponent

interface ViewComponentFactory {
  /**
   * Returns a new [ViewComponent] for the specified view.
   */
  fun createViewComponent(view: View): ViewComponent
}
