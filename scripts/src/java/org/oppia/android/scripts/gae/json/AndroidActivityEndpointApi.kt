package org.oppia.android.scripts.gae.json

import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface AndroidActivityEndpointApi {
  @GET("android_data?activity_type=classroom")
  fun fetchLatestClassroom(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeClassroom>>

  @GET("android_data?activity_type=exploration")
  fun fetchLatestExploration(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeExploration>>

  @GET("android_data?activity_type=exploration")
  fun fetchExplorationByVersion(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.NonLocalized>
  ): Call<Map<String, GaeExploration>>

  @GET("android_data?activity_type=story")
  fun fetchLatestStory(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeStory>>

  @GET("android_data?activity_type=story")
  fun fetchStoryByVersion(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.NonLocalized>
  ): Call<Map<String, GaeStory>>

  @GET("android_data?activity_type=skill")
  fun fetchLatestConceptCard(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeSkill>>

  @GET("android_data?activity_type=skill")
  fun fetchConceptCardByVersion(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.NonLocalized>
  ): Call<Map<String, GaeSkill>>

  @GET("android_data?activity_type=subtopic")
  fun fetchLatestRevisionCard(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeSubtopicPage>>

  @GET("android_data?activity_type=subtopic")
  fun fetchRevisionCardByVersion(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.NonLocalized>
  ): Call<Map<String, GaeSubtopicPage>>

  @GET("android_data?activity_type=learntopic")
  fun fetchLatestTopic(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.LatestVersion>
  ): Call<Map<String, GaeTopic>>

  @GET("android_data?activity_type=learntopic")
  fun fetchTopicByVersion(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.NonLocalized>
  ): Call<Map<String, GaeTopic>>

  @GET("android_data?activity_type=exp_translations")
  fun fetchExplorationTranslations(
    @Query("activities_data") request: AndroidActivityRequests<ActivityRequest.Localized>
  ): Call<Map<String, GaeEntityTranslation>>
}
