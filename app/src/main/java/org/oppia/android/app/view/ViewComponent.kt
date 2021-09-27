package org.oppia.android.app.view

import android.view.View

/**
 * Root subcomponent for custom views.
 *
 * Instances of this subcomponent should be created using [ViewComponentFactory].
 */
interface ViewComponent {
  /** Dagger builder for [ViewComponent]. */
  interface Builder {
    /**
     * Sets the root [View] that defines this component.
     *
     * @return this [Builder]
     */
    fun setView(view: View): Builder

    /** Returns a new [ViewComponent]. */
    fun build(): ViewComponent
  }
}
