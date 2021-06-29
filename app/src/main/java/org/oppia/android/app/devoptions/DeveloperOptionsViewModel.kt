package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(activity: AppCompatActivity) {
  private val forceCrashButtonClickListener = activity as ForceCrashButtonClickListener

  val developerOptionsList: List<DeveloperOptionsItemViewModel> by lazy {
    processDeveloperOptionsList()
  }

  private fun processDeveloperOptionsList(): List<DeveloperOptionsItemViewModel> {
    val itemViewModelList: MutableList<DeveloperOptionsItemViewModel> =
      mutableListOf(DeveloperOptionsModifyLessonProgressViewModel())
    itemViewModelList.add(DeveloperOptionsViewLogsViewModel())
    itemViewModelList.add(
      DeveloperOptionsOverrideAppBehaviorsViewModel(forceCrashButtonClickListener)
    )
    return itemViewModelList
  }
}
