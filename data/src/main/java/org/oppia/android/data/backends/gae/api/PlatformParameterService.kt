package org.oppia.android.data.backends.gae.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** Service that provides access to the Oppia platform parameter endpoint. */
interface PlatformParameterService {

  /**
   * This function returns a Map of Platform Parameters where the keys corresponds to parameter
   * names and values corresponds to their server value.
   * [Reference](https://github.com/oppia/oppia/blob/develop/core/controllers/platform_feature.py)
   *
   * @param version [String] - App version at the time of Build.
   * @param platformType [String] - Type of client from which the request is made. It should have
   * a default value of "Android" for Oppia-Android.
   *
   * @return [Map]<[String], [Any]> - Platform parameter values mapped to their names.
   */
  // TODO(#3506): Change the URL to point to the updated endpoint.
  @GET("platform_features_evaluation_handler")
  fun getPlatformParametersByVersion(
    @Query("app_version") version: String,
    @Query("platform_type") platformType: String = "Android"
  ): Call<Map<String, Any>>
}
