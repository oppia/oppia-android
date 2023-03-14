package org.oppia.android.scripts.gae.json

import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.LatestVersion
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.Localized
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.NonLocalized
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AndroidActivityHandlerService(private val apiSecret: String, private val baseUrl: String) {
  // TODO: Add batching support to this service, and use it upstream (when there's actual prod data to check against).
  private val httpClient by lazy {
    OkHttpClient.Builder().apply {
      addInterceptor(AuthorizationSecretAdderNetworkInterceptor(apiSecret))
      addInterceptor(JsonPrefixRemoverNetworkInterceptor())
    }.build()
  }
  private val retrofit by lazy {
    Retrofit.Builder().apply {
      baseUrl(baseUrl)
      client(httpClient)
      val moshi = MoshiFactory.createMoshi()
      addConverterFactory(MoshiStringConverterFactory(moshi))
      addConverterFactory(MoshiConverterFactory.create(moshi))
    }.build()
  }
  private val apiService by lazy { retrofit.create(AndroidActivityEndpointApi::class.java) }

  fun fetchLatestClassroomAsync(name: String): Deferred<GaeClassroom> =
    apiService.fetchLatestClassroom(LatestVersion(name).wrap()).resolveAsync(expectedId = name)

  fun fetchLatestExplorationAsync(id: String): Deferred<GaeExploration> =
    apiService.fetchLatestExploration(LatestVersion(id).wrap()).resolveAsync(id)

  fun fetchExplorationByVersionAsync(id: String, version: Int): Deferred<GaeExploration> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchExplorationByVersion(NonLocalized(id, version).wrap()).resolveAsync(id)
  }

  fun fetchLatestStoryAsync(id: String): Deferred<GaeStory> =
    apiService.fetchLatestStory(LatestVersion(id).wrap()).resolveAsync(id)

  fun fetchStoryByVersionAsync(id: String, version: Int): Deferred<GaeStory> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchStoryByVersion(NonLocalized(id, version).wrap()).resolveAsync(id)
  }

  fun fetchLatestConceptCardAsync(skillId: String): Deferred<GaeSkill> =
    apiService.fetchLatestConceptCard(LatestVersion(skillId).wrap()).resolveAsync(skillId)

  fun fetchConceptCardByVersionAsync(skillId: String, version: Int): Deferred<GaeSkill> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchConceptCardByVersion(
      NonLocalized(skillId, version).wrap()
    ).resolveAsync(skillId)
  }

  fun fetchLatestRevisionCardAsync(topicId: String, subtopicIndex: Int): Deferred<GaeSubtopicPage> {
    val subtopicId = "$topicId-$subtopicIndex"
    return apiService.fetchLatestRevisionCard(
      LatestVersion(subtopicId).wrap()
    ).resolveAsync(subtopicId)
  }

  fun fetchRevisionCardByVersionAsync(
    topicId: String,
    subtopicIndex: Int,
    version: Int
  ): Deferred<GaeSubtopicPage> {
    require(version >= 1) { "Version must be >= 1." }
    val subtopicId = "$topicId-$subtopicIndex"
    return apiService.fetchRevisionCardByVersion(
      NonLocalized(subtopicId, version).wrap()
    ).resolveAsync(subtopicId)
  }

  fun fetchLatestTopicAsync(id: String): Deferred<GaeTopic> =
    apiService.fetchLatestTopic(LatestVersion(id).wrap()).resolveAsync(id)

  fun fetchTopicByVersionAsync(id: String, version: Int): Deferred<GaeTopic> {
    require(version >= 1) { "Version must be >= 1." }
    return apiService.fetchTopicByVersion(NonLocalized(id, version).wrap()).resolveAsync(id)
  }

  fun fetchExplorationTranslationsAsync(
    explorationId: String,
    explorationVersion: Int,
    languageCode: String
  ): Deferred<GaeEntityTranslation> {
    require(explorationVersion >= 1) { "Exploration version must be >= 1." }
    return apiService.fetchExplorationTranslations(
      Localized(explorationId, explorationVersion, languageCode).wrap()
    ).resolveAsync(explorationId)
  }

  private fun <T> Call<Map<String, T>>.resolveAsync(expectedId: String): Deferred<T> {
    // Use the I/O dispatcher for blocking HTTP operations (since it's designed to handle blocking
    // operations that might otherwise stall a coroutine dispatcher).
    return CoroutineScope(Dispatchers.IO).async {
      println("Waiting for request to complete: ${request().url}...".redact())
      val result = execute()
      return@async if (result.isSuccessful) {
        val responses = checkNotNull(result.body()) {
          "Failed to receive body for request: ${request()}.".redact()
        }
        checkNotNull(responses[expectedId]) {
          "Missing expected ID $expectedId from responses: $responses.".redact()
        }
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
  private class JsonPrefixRemoverNetworkInterceptor : Interceptor {
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

  private class AuthorizationSecretAdderNetworkInterceptor(
    private val apiSecret: String
  ) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      return chain.proceed(
        chain.request().newBuilder().apply {
          // Augment the request's headers with the authorization token.
          addHeader("X-ApiKey", apiSecret)
        }.build()
      )
    }
  }

  // This is loosely based on MoshiConverterFactory, though it's set up to generate compact JSON
  // strings for GET requests (since MoshiConverterFactory doesn't support this directly).
  private class MoshiStringConverterFactory(private val moshi: Moshi): Converter.Factory() {
    override fun stringConverter(
      type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<*, String> {
      val jsonAnnotations = annotations.filter { annotation ->
        annotation.annotationClass.annotations.any { it is JsonQualifier }
      }.toSet()
      val adapter = moshi.adapter<Any>(type, jsonAnnotations)
      return Converter<Any, String> { adapter.toJson(it) }
    }
  }

  private companion object {
    private fun <T: ActivityRequest> T.wrap(): AndroidActivityRequests<T> =
      AndroidActivityRequests(requests = listOf(this@wrap))
  }
}
