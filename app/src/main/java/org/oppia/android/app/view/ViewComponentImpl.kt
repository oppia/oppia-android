package org.oppia.android.app.view

import android.view.View
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.customview.ChapterNotStartedContainerConstraintLayout
import org.oppia.android.app.customview.ContinueButtonView
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.customview.PromotedStoryCardView
import org.oppia.android.app.customview.SegmentedCircularProgressView
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsListView
import org.oppia.android.app.home.promotedlist.PromotedStoryListView
import org.oppia.android.app.player.state.DragDropSortInteractionView
import org.oppia.android.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.android.app.player.state.SelectionInteractionView

// TODO(#59): Restrict access to this implementation by introducing injectors in each view.

/** Implementation of [ViewComponent]. */
@Subcomponent
@ViewScope
interface ViewComponentImpl :
  ViewComponent,
  ChapterNotStartedContainerConstraintLayout.Injector,
  ComingSoonTopicsListView.Injector,
  ContinueButtonView.Injector,
  SelectionInteractionView.Injector,
  DragDropSortInteractionView.Injector,
  ImageRegionSelectionInteractionView.Injector,
  LessonThumbnailImageView.Injector,
  PromotedStoryCardView.Injector,
  PromotedStoryListView.Injector,
  SegmentedCircularProgressView.Injector {
  /** Implementation of [ViewComponent.Builder]. */
  @Subcomponent.Builder
  interface Builder : ViewComponent.Builder {
    @BindsInstance
    override fun setView(view: View): Builder

    override fun build(): ViewComponentImpl
  }
}
