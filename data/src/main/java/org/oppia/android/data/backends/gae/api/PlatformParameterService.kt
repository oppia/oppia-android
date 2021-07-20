package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaePlatformParameterList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** Service that provides access to the Oppia platform parameter endpoint. */
interface PlatformParameterService {

// TODO(#3506): Change the url to point to the correct endpoint when the backend is ready
  @GET("platform_features_evaluation_handler")
  fun getPlatformParametersByVersion(
    @Query("app_version") version: String,
    @Query("platform_type") platformType: String = "Android"
  ): Call<GaePlatformParameterList>
}
