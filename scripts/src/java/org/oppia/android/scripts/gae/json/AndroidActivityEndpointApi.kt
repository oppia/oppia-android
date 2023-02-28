package org.oppia.android.scripts.gae.json

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface AndroidActivityEndpointApi {
  @GET("android_data/{api_secret}?activity_type=classroom")
  fun fetchLatestClassroom(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") name: String
  ): Call<GaeClassroom>

  @GET("android_data/{api_secret}?activity_type=exploration")
  fun fetchLatestExploration(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String
  ): Call<GaeExploration>

  @GET("android_data/{api_secret}?activity_type=exploration")
  fun fetchExplorationByVersion(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String,
    @Query("activity_version") version: Int
  ): Call<GaeExploration>

  @GET("android_data/{api_secret}?activity_type=story")
  fun fetchLatestStory(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String
  ): Call<GaeStory>

  @GET("android_data/{api_secret}?activity_type=story")
  fun fetchStoryByVersion(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String,
    @Query("activity_version") version: Int
  ): Call<GaeStory>

  @GET("android_data/{api_secret}?activity_type=skill")
  fun fetchLatestConceptCard(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") skillId: String
  ): Call<GaeSkill>

  @GET("android_data/{api_secret}?activity_type=skill")
  fun fetchConceptCardByVersion(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") skillId: String,
    @Query("activity_version") version: Int
  ): Call<GaeSkill>

  @GET("android_data/{api_secret}?activity_type=subtopic")
  fun fetchLatestRevisionCard(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") qualifiedSubtopicId: String
  ): Call<GaeSubtopicPage>

  @GET("android_data/{api_secret}?activity_type=subtopic")
  fun fetchRevisionCardByVersion(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") qualifiedSubtopicId: String,
    @Query("activity_version") version: Int
  ): Call<GaeSubtopicPage>

  @GET("android_data/{api_secret}?activity_type=learntopic")
  fun fetchLatestTopic(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String
  ): Call<GaeTopic>

  @GET("android_data/{api_secret}?activity_type=learntopic")
  fun fetchTopicByVersion(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") id: String,
    @Query("activity_version") version: Int
  ): Call<GaeTopic>

  @GET("android_data/{api_secret}?activity_type=exp_translations")
  fun fetchExplorationTranslations(
    @Path("api_secret") apiSecret: String,
    @Query("activity_id") explorationId: String,
    @Query("activity_version") explorationVersion: Int,
    @Query("language_code") languageCode: String
  ): Call<GaeEntityTranslation>
}
