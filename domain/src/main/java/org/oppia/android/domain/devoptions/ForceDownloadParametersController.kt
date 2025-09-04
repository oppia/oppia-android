package org.oppia.android.domain.devoptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for force-downloading platform parameters. */
@Singleton
class ForceDownloadParametersController @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
) {

  private val isDownloadEnabled = MutableLiveData(true)

  /** Initiates a force download of remote platform parameters. */
  fun downloadRemoteParameters(): DataProvider<Unit> {
    setForceDownloadEnabled(false)
    return platformParameterControllerDebugImpl.downloadRemoteParameters()
  }

  /** Returns whether the force download action is currently enabled. */
  fun getForceDownloadEnabled(): LiveData<Boolean> {
    return isDownloadEnabled
  }

  /** Updates the enabled state of the force download action. */
  fun setForceDownloadEnabled(enabled: Boolean) {
    isDownloadEnabled.value = enabled
  }
}
