package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeFeatureFlag
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlatformParameterDebugService : PlatformParameterService {
  @GET("android_feature_flags")
  fun getFeatureFlags(
    @Query("android_enable_fast_language_switching_in_lesson")
    overrideEnableEnableFastLanguageSwitchingInLesson: Boolean? = null
  ): Call<List<GaeFeatureFlag>>

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
