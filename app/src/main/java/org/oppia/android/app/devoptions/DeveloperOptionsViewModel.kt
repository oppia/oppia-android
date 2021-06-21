package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
) {
  private val routeToMarkChaptersCompletedListener =
    activity as RouteToMarkChaptersCompletedListener
  private val routeToMarkStoriesCompletedListener =
    activity as RouteToMarkStoriesCompletedListener
  private var internalProfileId: Int = -1
  val selectedFragmentIndex = ObservableField<Int>(1)

  val developerOptionsList: List<DeveloperOptionsItemViewModel> by lazy {
    processDeveloperOptionsList()
  }

  private fun processDeveloperOptionsList(): List<DeveloperOptionsItemViewModel> {
    val itemViewModelList: MutableList<DeveloperOptionsItemViewModel> =
      mutableListOf(
        DeveloperOptionsModifyLessonProgressViewModel(
          routeToMarkChaptersCompletedListener,
          routeToMarkStoriesCompletedListener
        )
      )
    itemViewModelList.add(DeveloperOptionsViewLogsViewModel())
    itemViewModelList.add(DeveloperOptionsOverrideAppBehaviorsViewModel())
    return itemViewModelList
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }
}
