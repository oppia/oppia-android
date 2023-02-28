package org.oppia.android.scripts.gae.json

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AndroidActivityHandlerService(private val apiSecret: String, private val baseUrl: String) {
  // TODO: Add an interceptor for secret to add to header.
  private val httpClient by lazy {
    OkHttpClient.Builder().apply {
      addInterceptor(JsonPrefixNetworkInterceptor())
    }.build()
  }
  private val retrofit by lazy {
    Retrofit.Builder().apply {
      baseUrl(baseUrl)
      client(httpClient)
      addConverterFactory(MoshiConverterFactory.create(MoshiFactory.createMoshi()))
    }.build()
  }
  private val apiService by lazy { retrofit.create(AndroidActivityEndpointApi::class.java) }

  fun fetchLatestClassroomAsync(name: String): Deferred<GaeClassroom> =
    apiService.fetchLatestClassroom(apiSecret, name).resolveAsync()

  fun fetchLatestExplorationAsync(id: String): Deferred<GaeExploration> =
    apiService.fetchLatestExploration(apiSecret, id).resolveAsync()

  fun fetchExplorationByVersionAsync(id: String, version: Int): Deferred<GaeExploration> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchExplorationByVersion(apiSecret, id, version).resolveAsync()
  }

  fun fetchLatestStoryAsync(id: String): Deferred<GaeStory> =
    apiService.fetchLatestStory(apiSecret, id).resolveAsync()

  fun fetchStoryByVersionAsync(id: String, version: Int): Deferred<GaeStory> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchStoryByVersion(apiSecret, id, version).resolveAsync()
  }

  fun fetchLatestConceptCardAsync(skillId: String): Deferred<GaeSkill> =
    apiService.fetchLatestConceptCard(apiSecret, skillId).resolveAsync()

  fun fetchConceptCardByVersionAsync(skillId: String, version: Int): Deferred<GaeSkill> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchConceptCardByVersion(apiSecret, skillId, version).resolveAsync()
  }

  fun fetchLatestRevisionCardAsync(topicId: String, subtopicIndex: Int): Deferred<GaeSubtopicPage> {
    return apiService.fetchLatestRevisionCard(
      apiSecret, qualifiedSubtopicId = "$topicId-$subtopicIndex"
    ).resolveAsync()
  }

  fun fetchRevisionCardByVersionAsync(
    topicId: String,
    subtopicIndex: Int,
    version: Int
  ): Deferred<GaeSubtopicPage> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchRevisionCardByVersion(
      apiSecret, qualifiedSubtopicId = "$topicId-$subtopicIndex", version
    ).resolveAsync()
  }

  fun fetchLatestTopicAsync(id: String): Deferred<GaeTopic> =
    apiService.fetchLatestTopic(apiSecret, id).resolveAsync()

  fun fetchTopicByVersionAsync(id: String, version: Int): Deferred<GaeTopic> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchTopicByVersion(apiSecret, id, version).resolveAsync()
  }

  fun fetchExplorationTranslationsAsync(
    explorationId: String,
    explorationVersion: Int,
    languageCode: String
  ): Deferred<GaeEntityTranslation> {
    require(explorationVersion >= 1) { "Exploration version must be >= 1." }
    return apiService.fetchExplorationTranslations(
      apiSecret, explorationId, explorationVersion, languageCode
    ).resolveAsync()
  }

  private fun <T> Call<T>.resolveAsync(): Deferred<T> {
    // Use the I/O dispatcher for blocking HTTP operations (since it's designed to handle blocking
    // operations that might otherwise stall a coroutine dispatcher).
    return CoroutineScope(Dispatchers.IO).async {
      println("Waiting for request to complete: ${request().url}...".redact())
      val result = execute()
      return@async if (result.isSuccessful) {
        checkNotNull(result.body()) { "Failed to receive body for request: ${request()}.".redact() }
      } else error("Failed to call: ${request()}. Encountered failure:\n$result.".redact())
    }
  }

  private fun String.redact(): String = replace(apiSecret, "<redacted_secret>")

  /**
   * Interceptor on top of Retrofit to modify requests and response.
   *
   * The interceptor removes the [XSSI_PREFIX] from every Oppia backend response to produce valid
   * JSON.
   */
  private class JsonPrefixNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      val originalResponse = chain.proceed(chain.request())
      return originalResponse.newBuilder().apply {
        body(originalResponse.body?.stripXssiPrefix())
      }.build()
    }

    private companion object {
      private const val XSSI_PREFIX = ")]}'"

      private fun ResponseBody.stripXssiPrefix(): ResponseBody =
        string().removePrefix(XSSI_PREFIX).trimStart().toResponseBody(contentType())
    }
  }
}
