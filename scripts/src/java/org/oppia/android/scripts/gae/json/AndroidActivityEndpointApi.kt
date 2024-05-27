package org.oppia.android.scripts.gae.json

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface AndroidActivityEndpointApi {
  @GET("android_data?activity_type=classroom")
  fun fetchLatestClassroom(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeClassroom>>>

  @GET("android_data?activity_type=exploration")
  fun fetchLatestExploration(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeExploration>>>

  @GET("android_data?activity_type=exploration")
  fun fetchExplorationByVersion(
    @Query("activities_data") request: AndroidActivityRequests.NonLocalized
  ): Call<List<VersionedStructure<GaeExploration>>>

  @GET("android_data?activity_type=story")
  fun fetchLatestStory(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeStory>>>

  @GET("android_data?activity_type=story")
  fun fetchStoryByVersion(
    @Query("activities_data") request: AndroidActivityRequests.NonLocalized
  ): Call<List<VersionedStructure<GaeStory>>>

  @GET("android_data?activity_type=skill")
  fun fetchLatestConceptCard(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeSkill>>>

  @GET("android_data?activity_type=skill")
  fun fetchConceptCardByVersion(
    @Query("activities_data") request: AndroidActivityRequests.NonLocalized
  ): Call<List<VersionedStructure<GaeSkill>>>

  @GET("android_data?activity_type=subtopic")
  fun fetchLatestRevisionCard(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeSubtopicPage>>>

  @GET("android_data?activity_type=subtopic")
  fun fetchRevisionCardByVersion(
    @Query("activities_data") request: AndroidActivityRequests.NonLocalized
  ): Call<List<VersionedStructure<GaeSubtopicPage>>>

  @GET("android_data?activity_type=learntopic")
  fun fetchLatestTopic(
    @Query("activities_data") request: AndroidActivityRequests.Latest
  ): Call<List<VersionedStructure<GaeTopic>>>

  @GET("android_data?activity_type=learntopic")
  fun fetchTopicByVersion(
    @Query("activities_data") request: AndroidActivityRequests.NonLocalized
  ): Call<List<VersionedStructure<GaeTopic>>>

  @GET("android_data?activity_type=exp_translations")
  fun fetchExplorationTranslations(
    @Query("activities_data") request: AndroidActivityRequests.Localized
  ): Call<List<VersionedStructure<GaeEntityTranslations>>>
}
