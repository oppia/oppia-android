package org.oppia.android.domain.devoptions

import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for force-downloading platform parameters. */
@Singleton
class ForceDownloadParametersController @Inject constructor(
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
) {
  /** Initiates a force download of remote platform parameters. */
  fun downloadRemoteParameters(): DataProvider<Unit> {
    return platformParameterControllerDebugImpl.downloadRemoteParameters()
  }
}
