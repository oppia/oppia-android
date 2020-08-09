package org.oppia.app.shim

import android.view.View
import org.oppia.app.view.ViewComponent

interface ViewComponentFactory {
  fun createViewComponent(view: View): ViewComponent
}