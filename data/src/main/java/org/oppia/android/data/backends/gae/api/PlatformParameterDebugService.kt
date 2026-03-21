package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeFeatureFlag
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Debug-specific [PlatformParameterService] that provides access to the Oppia platform parameter
 * and feature flag endpoints with additional customization support.
 *
 * This interface extends [PlatformParameterService] to provide overloaded endpoints that allow for
 * query-parameter-based overrides of platform parameters and feature flags. This is useful for
 * testing and debugging that the app is correctly download platform parameters and feature flags
 * without requiring changing the actual values delivered from Oppia web.
 */
interface PlatformParameterDebugService : PlatformParameterService {
  /**
   * Retrieves a list of feature flags from the server, optionally overriding the value for
   * enabling fast language switching in lessons.
   *
   * Unlike its variant in [PlatformParameterService], this method will prompt the server to return
   * the provided value rather than its default.
   *
   * @param overrideEnableEnableFastLanguageSwitchingInLesson an optional boolean to override the
   *     corresponding feature flag in the server's response, or null to receive the default value
   *     from the server
   * @return a [Call] providing a list of feature flags configured on the server
   */
  @GET("android_feature_flags")
  fun getFeatureFlags(
    @Query("android_enable_fast_language_switching_in_lesson")
    overrideEnableEnableFastLanguageSwitchingInLesson: Boolean? = null
  ): Call<List<GaeFeatureFlag>>

  /**
   * Retrieves a list of platform parameters from the server, with optional overrides for various
   * app update and support version requirements.
   *
   * Unlike its variant in [PlatformParameterService], this method will prompt the server to return
   * the provided value(s) rather than its default(s).
   *
   * @param overrideAndroidMinVersionCodeForRecommendingAppUpdate an optional integer to override
   *     the minimum version code required for recommending an app update, or null to receive the
   *     default value from the server
   * @param overrideAndroidMinSupportedVersionCode an optional integer to override the minimum
   *     supported version code of the app, or null to receive the default value from the server
   * @param overrideAndroidMinSupportApiLevel an optional integer to override the minimum supported
   *     Android API level, or null to receive the default value from the server
   * @return a [Call] providing a list of platform parameters configured on the server
   */
  @GET("android_platform_parameters")
  fun getPlatformParameters(
    @Query("android_min_version_code_for_recommending_app_update")
    overrideAndroidMinVersionCodeForRecommendingAppUpdate: Int? = null,
    @Query("android_min_supported_version_code")
    overrideAndroidMinSupportedVersionCode: Int? = null,
    @Query("android_min_supported_api_level")
    overrideAndroidMinSupportApiLevel: Int? = null,
  ): Call<List<GaePlatformParameter>>
}
