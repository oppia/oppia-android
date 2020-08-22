package org.oppia.app.shim

import android.view.View
import org.oppia.app.view.ViewComponent

interface ViewComponentFactory {
  /**
   * Returns a new [ViewComponent] for the specified view.
   */
  fun createViewComponent(view: View): ViewComponent
}
