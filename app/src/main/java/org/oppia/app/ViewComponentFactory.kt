package org.oppia.app

import android.view.View
import org.oppia.app.view.ViewComponent

interface ViewComponentFactory {

  fun createViewComponent(view: View): ViewComponent
}
