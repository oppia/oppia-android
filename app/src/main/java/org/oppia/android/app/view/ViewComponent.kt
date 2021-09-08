package org.oppia.android.app.view

import android.view.View

interface ViewComponent {
  interface Builder {
    fun setView(view: View): Builder

    fun build(): ViewComponent
  }
}
