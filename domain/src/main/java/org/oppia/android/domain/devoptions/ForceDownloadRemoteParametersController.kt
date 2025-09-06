package org.oppia.android.domain.devoptions

import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for force-downloading remote platform parameters and feature flags. */
@Singleton
class ForceDownloadRemoteParametersController @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
) {
  /** Initiates a force download of remote platform parameters. */
  fun downloadRemoteParameters(): DataProvider<Unit> {
    return platformParameterControllerDebugImpl.downloadRemoteParameters()
  }

  /** Cancels an ongoing force download of remote platform parameters. */
  fun cancelRemoteParameterDownload(): Boolean {
    return platformParameterControllerDebugImpl.cancelRemoteParameterDownload()
  }
}
