package org.oppia.android.app.devoptions.featureflags

import androidx.annotation.ColorInt
import androidx.databinding.ObservableField
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/** [ViewModel] for displaying a feature flag for the recycler view in [FeatureFlagsFragment]. */
class FeatureFlagItemViewModel @Inject constructor(
  val featureFlagId: FeatureFlagId,
  val currentValue: Boolean,
  val syncStatus: SyncStatus,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler

) : ObservableViewModel() {

  val isChecked: ObservableField<Boolean> = ObservableField(currentValue)
  val featureFlagDisplayName: ObservableField<String> =
    ObservableField(getFeatureFlagDisplayName(featureFlagId))
  val syncStatusDisplayText: ObservableField<String> =
    ObservableField(getSyncStatusText())

  var onToggleCallback: ((FeatureFlagId, Boolean) -> Unit)? = null

  fun onUserToggle() {
    isChecked.set(!isChecked.get()!!)
    onToggleCallback?.invoke(featureFlagId, isChecked.get()!!)
  }

  @ColorInt
  val backgroundColor: Int = retrieveBackgroundColor().toInt()
  private fun getFeatureFlagDisplayName(
    id: FeatureFlagId
  ): String {
    return machineLocale.run {
      when (id) {
        FeatureFlagId.UNRECOGNIZED,
        FeatureFlagId.FEATURE_FLAG_ID_UNSPECIFIED -> "Unknown Feature"

        else ->
          id.name.toMachineLowerCase()
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
      SyncStatus.NOT_SYNCED_FROM_SERVER -> 0xFF00645C
      SyncStatus.SYNCED_FROM_SERVER -> 0xFFBE563C
      else -> 0xFF00645C
    }
  }
}
