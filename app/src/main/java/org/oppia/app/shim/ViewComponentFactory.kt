package org.oppia.app.shim

import android.view.View
import org.oppia.app.view.ViewComponent

/** Removes [InjectableFragment] dependency from Views */
// TODO(#1619): Remove file post-Gradle
interface ViewComponentFactory {
  fun createViewComponent(view: View): ViewComponent
}
