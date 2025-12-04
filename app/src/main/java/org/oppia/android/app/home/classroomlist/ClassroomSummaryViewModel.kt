package org.oppia.android.app.home.classroomlist

import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.EphemeralClassroomSummary
import org.oppia.android.domain.translation.TranslationController
import java.util.Objects

/** The view model corresponding to individual classroom summaries in the classroom summary RecyclerView. */
class ClassroomSummaryViewModel(
  private val classroomSummaryClickListener: ClassroomSummaryClickListener,
  ephemeralClassroomSummary: EphemeralClassroomSummary,
  val entityType: String,
  translationController: TranslationController,
) : HomeItemViewModel() {
  /** The [ClassroomSummary] retrieved from the [EphemeralClassroomSummary]. */
  val classroomSummary: ClassroomSummary = ephemeralClassroomSummary.classroomSummary

  /** Lazy-loaded title extracted using the [TranslationController]. */
  val title: String by lazy {
    translationController.extractString(
      ephemeralClassroomSummary.classroomSummary.classroomTitle,
      ephemeralClassroomSummary.writtenTranslationContext
    )
  }

  /** Handles the click event for a [ClassroomSummary] by invoking the click listener. */
  fun handleClassroomClick() {
    classroomSummaryClickListener.onClassroomSummaryClicked(classroomSummary)
  }

  // Overriding equals is needed so that DataProvider combine functions used in the
  // ClassroomListViewModel will only rebind when the actual data in the data list changes,
  // rather than when the ViewModel object changes.
  override fun equals(other: Any?): Boolean {
    return other is ClassroomSummaryViewModel
  }

  override fun hashCode() = Objects.hash()
}
