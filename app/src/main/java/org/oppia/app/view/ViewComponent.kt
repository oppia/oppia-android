package org.oppia.app.view

import android.view.View
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.player.state.DragDropSortInteractionView
import org.oppia.app.player.state.SelectionInteractionView

/** Root subcomponent for custom views. */
@Subcomponent
@ViewScope
interface ViewComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun setView(view: View): Builder

    fun build(): ViewComponent
  }

  fun inject(selectionInteractionView: SelectionInteractionView)
  fun inject(dragDropSortInteractionView: DragDropSortInteractionView)
}
