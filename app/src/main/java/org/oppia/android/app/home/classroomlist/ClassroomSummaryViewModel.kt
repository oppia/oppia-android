package org.oppia.android.app.home.classroomlist

import java.util.Objects
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.model.EphemeralClassroomSummary
import org.oppia.android.domain.translation.TranslationController

/** The view model corresponding to individual classroom summaries in the classroom summary RecyclerView. */
class ClassroomSummaryViewModel(
  ephemeralClassroomSummary: EphemeralClassroomSummary,
  translationController: TranslationController
) : HomeItemViewModel() {

  val title: String by lazy {
    translationController.extractString(
      ephemeralClassroomSummary.classroomSummary.classroomTitle,
      ephemeralClassroomSummary.writtenTranslationContext
    )
  }

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is ClassroomSummaryViewModel
  }

  override fun hashCode() = Objects.hash()
}
