package org.oppia.android.app.devoptions.platformparameters

import androidx.annotation.ColorInt
import androidx.databinding.ObservableField
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale

class PlatformParameterItemViewModel(
  val platformParameterId: PlatformParameterId,
  val currentValue: PlatformParameterValue,
  val syncStatus: SyncStatus,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  val isChecked = ObservableField(currentValue.boolean)
  val isInputVisible = ObservableField(!currentValue.hasBoolean())
  val platformParameterDisplayText = ObservableField(getPlatformParameterDisplayName())
  val syncStatusDisplayText = ObservableField(getSyncStatusText())
  val inputValue = ObservableField(
    if (currentValue.hasString()) currentValue.string
    else if (currentValue.hasInteger()) currentValue.integer.toString()
    else ""
  )

  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor().toInt()

  private fun getPlatformParameterDisplayName(): String {
    return machineLocale.run {
      when (platformParameterId) {
        PlatformParameterId.UNRECOGNIZED,
        PlatformParameterId.PLATFORM_PARAMETER_ID_UNSPECIFIED -> "Unknown"

        else ->
          platformParameterId.name.toMachineLowerCase()
            .split("_")
            .joinToString(" ") { it.capitalizeForMachines() }
      }
    }
  }

  private fun getSyncStatusText(): String {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED ->
        resourceHandler.getStringInLocale(R.string.feature_flag_unknown_sync_status)

      SyncStatus.NOT_SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.feature_flag_default_sync_status)

      SyncStatus.SYNCED_FROM_SERVER ->
        resourceHandler.getStringInLocale(R.string.feature_flag_server_sync_status)

      else ->
        resourceHandler.getStringInLocale(R.string.feature_flag_unknown_sync_status)
    }
  }

  @ColorInt
  private fun retrieveBackgroundColor(): Long {
    return when (syncStatus) {
      SyncStatus.SYNC_STATUS_UNSPECIFIED -> 0xFF00645C
      SyncStatus.NOT_SYNCED_FROM_SERVER -> 0xFFBE563C
      SyncStatus.SYNCED_FROM_SERVER -> 0xFF00645C
      else -> 0xFF00645C
    }
  }
}
