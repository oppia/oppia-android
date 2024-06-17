package org.oppia.android.app.home.classroomlist

import ClassroomListViewModel
import java.util.Objects
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.EphemeralClassroomSummary
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.domain.translation.TranslationController

/** The view model corresponding to individual classroom summaries in the classroom summary RecyclerView. */
class ClassroomSummaryViewModel(
  viewModel: ClassroomListViewModel,
  ephemeralClassroomSummary: EphemeralClassroomSummary,
  translationController: TranslationController
) : HomeItemViewModel() {

  val routeToTopic = viewModel as RouteToTopic

  val classroomId = ephemeralClassroomSummary.classroomSummary.classroomId

  val title: String by lazy {
    translationController.extractString(
      ephemeralClassroomSummary.classroomSummary.classroomTitle,
      ephemeralClassroomSummary.writtenTranslationContext
    )
  }

  val thumbnailResourceId: Int by lazy {
    when (ephemeralClassroomSummary.classroomSummary.classroomThumbnail.thumbnailGraphic) {
      LessonThumbnailGraphic.SCIENCE_CLASSROOM -> R.drawable.ic_science_classroom
      LessonThumbnailGraphic.MATHS_CLASSROOM -> R.drawable.ic_maths_classroom
      LessonThumbnailGraphic.ENGLISH_CLASSROOM -> R.drawable.ic_english_classroom
      else -> R.drawable.ic_maths_classroom
    }
  }

  fun handleClassroomClick(classroomId: String) {
    routeToTopic.onClassroomClicked(classroomId)
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is ClassroomSummaryViewModel
  }

  override fun hashCode() = Objects.hash()
}

interface RouteToTopic {
  fun onClassroomClicked(classroomId: String)
}
