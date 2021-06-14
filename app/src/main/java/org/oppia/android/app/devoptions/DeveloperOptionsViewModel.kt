package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** [ViewModel] for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var userProfileId: ProfileId
  val selectedFragmentIndex = ObservableField<Int>(1)

  private val deviceSettingsLiveData: LiveData<DeviceSettings> by lazy {
    Transformations.map(
      profileManagementController.getDeviceSettings().toLiveData(),
      ::processGetDeviceSettingsResult
    )
  }

  val developerOptionsLiveData: LiveData<List<DeveloperOptionsItemViewModel>> by lazy {
    Transformations.map(deviceSettingsLiveData, ::processDeveloperOptionsList)
  }

  private fun processGetDeviceSettingsResult(
    deviceSettingsResult: AsyncResult<DeviceSettings>
  ): DeviceSettings {
    return deviceSettingsResult.getOrDefault(DeviceSettings.getDefaultInstance())
  }

  private fun processDeveloperOptionsList(
    deviceSettings: DeviceSettings
  ): List<DeveloperOptionsItemViewModel> {
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