package org.oppia.android.app.view

import android.view.View
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.android.app.customview.ChapterNotStartedContainerConstraintLayout
import org.oppia.android.app.customview.ContinueButtonView
import org.oppia.android.app.customview.LessonThumbnailImageView
import org.oppia.android.app.customview.OppiaCurveBackgroundView
import org.oppia.android.app.customview.PromotedStoryCardView
import org.oppia.android.app.customview.SegmentedCircularProgressView
import org.oppia.android.app.home.promotedlist.ComingSoonTopicsListView
import org.oppia.android.app.home.promotedlist.PromotedStoryListView
import org.oppia.android.app.player.state.DragDropSortInteractionView
import org.oppia.android.app.player.state.ImageRegionSelectionInteractionView
import org.oppia.android.app.player.state.SelectionInteractionView
import org.oppia.android.app.profile.ProfileListView
import org.oppia.android.app.survey.SurveyMultipleChoiceOptionView
import org.oppia.android.app.survey.SurveyNpsItemOptionView

// TODO(#59): Restrict access to this implementation by introducing injectors in each view.

/** Implementation of [ViewComponent]. */
@Subcomponent
@ViewScope
interface ViewComponentImpl : ViewComponent {
  /** Implementation of [ViewComponent.Builder]. */
  @Subcomponent.Builder
  interface Builder : ViewComponent.Builder {
    @BindsInstance
    override fun setView(view: View): Builder

    override fun build(): ViewComponentImpl
  }

  fun inject(chapterNotStartedContainerConstraintLayout: ChapterNotStartedContainerConstraintLayout)
  fun inject(comingSoonTopicsListView: ComingSoonTopicsListView)
  fun inject(continueButtonView: ContinueButtonView)
  fun inject(selectionInteractionView: SelectionInteractionView)
  fun inject(dragDropSortInteractionView: DragDropSortInteractionView)
  fun inject(imageRegionSelectionInteractionView: ImageRegionSelectionInteractionView)
  fun inject(lessonThumbnailImageView: LessonThumbnailImageView)
  fun inject(promotedStoryCardView: PromotedStoryCardView)
  fun inject(promotedStoryListView: PromotedStoryListView)
  fun inject(segmentedCircularProgressView: SegmentedCircularProgressView)
  fun inject(oppiaCurveBackgroundView: OppiaCurveBackgroundView)
  fun inject(surveyMultipleChoiceOptionView: SurveyMultipleChoiceOptionView)
  fun inject(surveyNpsItemOptionView: SurveyNpsItemOptionView)
  fun inject(profileListView: ProfileListView)
}
