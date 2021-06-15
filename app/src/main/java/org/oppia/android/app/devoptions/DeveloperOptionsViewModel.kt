package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId

/** [ViewModel] for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
) {
  private lateinit var userProfileId: ProfileId
  val selectedFragmentIndex = ObservableField<Int>(1)

  val developerOptionsList: List<DeveloperOptionsItemViewModel> by lazy {
    processDeveloperOptionsList()
  }

  private fun processDeveloperOptionsList(): List<DeveloperOptionsItemViewModel> {
    val itemViewModelList: MutableList<DeveloperOptionsItemViewModel> =
      mutableListOf(DeveloperOptionsModifyLessonProgressViewModel())
    itemViewModelList.add(DeveloperOptionsViewLogsViewModel())
    itemViewModelList.add(DeveloperOptionsOverrideAppBehaviorsViewModel())
    return itemViewModelList
  }

  fun setProfileId(profileId: ProfileId) {
    userProfileId = profileId
  }
}