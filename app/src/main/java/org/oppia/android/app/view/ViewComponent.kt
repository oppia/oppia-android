package org.oppia.android.app.view

import android.view.View
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.player.state.DragDropSortInteractionView
import org.oppia.android.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.android.app.player.state.SelectionInteractionView

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
  fun inject(lessonThumbnailImageView: LessonThumbnailImageView)
}
