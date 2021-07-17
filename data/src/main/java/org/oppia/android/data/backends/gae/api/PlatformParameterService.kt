package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaePlatformParameters
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** Service that provides access to platform parameter endpoint. */
interface PlatformParameterService {

// TODO("Change the url to point to the correct endpoint when the backend is ready")
  @GET("platform_features_evaluation_handler")
  fun getPlatformParametersByVersion(
    @Query("app_version") version: String
  ): Call<GaePlatformParameters>
}
