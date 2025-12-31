package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeFeatureFlag
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import retrofit2.Call
import retrofit2.http.GET

/** Service that provides access to the Oppia platform parameter endpoint. */
interface PlatformParameterService {
  /**
   * Retrieves map of platform parameters where the keys corresponds to parameter names and values
   * corresponds to their server-provided value.
   *
   * @return platform parameter values mapped to their names
   */
  @GET("android_feature_flags")
  fun getFeatureFlags(): Call<List<GaeFeatureFlag>>

  /**
   * Retrieves map of platform parameters where the keys corresponds to parameter names and values
   * corresponds to their server-provided value.
   *
   * @return platform parameter values mapped to their names
   */
  @GET("android_platform_parameters")
  fun getPlatformParameters(): Call<List<GaePlatformParameter>>
}
