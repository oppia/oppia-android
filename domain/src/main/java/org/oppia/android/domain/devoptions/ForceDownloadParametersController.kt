package org.oppia.android.domain.devoptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForceDownloadParametersController @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
) {

  private val isDownloadEnabled = MutableLiveData(true)

  fun downloadRemoteParameters(): DataProvider<Unit> {
    setForceDownloadEnabled(false)
    return platformParameterControllerDebugImpl.downloadRemoteParameters()
  }

  fun getForceDownloadEnabled(): LiveData<Boolean> {
    return isDownloadEnabled
  }

  fun setForceDownloadEnabled(enabled: Boolean) {
    isDownloadEnabled.value = enabled
  }
}
