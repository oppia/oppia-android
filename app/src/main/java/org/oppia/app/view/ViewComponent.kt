package org.oppia.app.view

import android.view.View
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.player.state.DragDropSortInteractionView
import org.oppia.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.app.player.state.SelectionInteractionView
import org.oppia.app.profile.ProfileInputView

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
  fun inject(imageRegionSelectionInteractionView: ImageRegionSelectionInteractionView)
  fun inject(profileInputView: ProfileInputView)
}
