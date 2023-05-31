package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.rawType
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Request
import okio.Buffer
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.LatestVersion
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.Localized
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.NonLocalized
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AndroidActivityHandlerService(
  private val apiSecret: String,
  private val baseUrl: String,
  private val cacheDir: File?,
  private val forceCacheLoad: Boolean,
  private val dispatcher: CoroutineDispatcher
) {
  private val memoizedRawResponses = mutableMapOf<RequestMetadata, String>()

  private val httpClient by lazy {
    OkHttpClient.Builder().apply {
      addInterceptor(AuthorizationSecretAdderNetworkInterceptor(apiSecret))
      addInterceptor(JsonPrefixRemoverNetworkInterceptor(memoizedRawResponses))
      readTimeout(/* timeout = */ 5, /* unit = */ TimeUnit.MINUTES)
      retryOnConnectionFailure(true)
    }.build()
  }
  private val moshi by lazy { MoshiFactory.createMoshi() }
  private val retrofit by lazy {
    Retrofit.Builder().apply {
      baseUrl(baseUrl)
      client(httpClient)
      addConverterFactory(MoshiRequestsStringConverterFactory(moshi))
      addConverterFactory(MoshiConverterFactory.create(moshi))
    }.build()
  }
  private val apiService by lazy { retrofit.create(AndroidActivityEndpointApi::class.java) }

  fun fetchLatestClassroomAsync(name: String): Deferred<GaeClassroom> {
    return fetchLatestFromServiceAsync(
      type = "classroom", id = name, fetch = apiService::fetchLatestClassroom
    )
  }

  fun fetchLatestExplorationAsync(id: String): Deferred<GaeExploration> {
    return fetchLatestFromServiceAsync(
      type = "exploration", id = id, fetch = apiService::fetchLatestExploration
    )
  }

  fun fetchExplorationByVersionsAsync(
    id: String, versions: List<Int>
  ): Deferred<List<GaeExploration>> {
    return fetchVersionedFromServiceAsync(
      type = "exploration",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchExplorationByVersion
    )
  }

  fun fetchLatestStoryAsync(id: String): Deferred<GaeStory> =
    fetchLatestFromServiceAsync(type = "story", id = id, fetch = apiService::fetchLatestStory)

  fun fetchStoryByVersionsAsync(id: String, versions: List<Int>): Deferred<List<GaeStory>> {
    return fetchVersionedFromServiceAsync(
      type = "story",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchStoryByVersion
    )
  }

  fun fetchLatestConceptCardAsync(skillId: String): Deferred<GaeSkill> {
    return fetchLatestFromServiceAsync(
      type = "concept_card", id = skillId, fetch = apiService::fetchLatestConceptCard
    )
  }

  fun fetchConceptCardByVersionsAsync(
    skillId: String, versions: List<Int>
  ): Deferred<List<GaeSkill>> {
    return fetchVersionedFromServiceAsync(
      type = "concept_card",
      id = skillId,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchConceptCardByVersion
    )
  }

  fun fetchLatestRevisionCardAsync(topicId: String, subtopicIndex: Int): Deferred<GaeSubtopicPage> {
    return fetchLatestFromServiceAsync(
      type = "revision_card",
      id = "$topicId-$subtopicIndex",
      fetch = apiService::fetchLatestRevisionCard
    )
  }

  fun fetchRevisionCardByVersionsAsync(
    topicId: String,
    subtopicIndex: Int,
    versions: List<Int>
  ): Deferred<List<GaeSubtopicPage>> {
    return fetchVersionedFromServiceAsync(
      type = "revision_card",
      id = "$topicId-$subtopicIndex",
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchRevisionCardByVersion
    )
  }

  fun fetchLatestTopicAsync(id: String): Deferred<GaeTopic> {
    return fetchLatestFromServiceAsync(
      type = "topic", id = id, fetch = apiService::fetchLatestTopic
    )
  }

  fun fetchTopicByVersionsAsync(id: String, versions: List<Int>): Deferred<List<GaeTopic>> {
    return fetchVersionedFromServiceAsync(
      type = "topic",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchTopicByVersion
    )
  }

  fun fetchExplorationTranslationsAsync(
    explorationId: String,
    explorationVersion: Int,
    languageCode: String
  ): Deferred<GaeEntityTranslation> {
    val fullFetch = fetchVersionedFromServiceAsync(
      type = "exploration_translations",
      id = explorationId,
      versions = listOf(explorationVersion),
      createRequest = { id, version -> Localized(id, version, languageCode) },
      createRequests = AndroidActivityRequests::Localized,
      fetch = apiService::fetchExplorationTranslations
    )
    return CoroutineScope(dispatcher).async { fullFetch.await().single() }
  }

  private fun <S : VersionedStructure> Call<Map<String, S>>.resolveAsyncVersionsAsync(
    expectedId: String, expectedVersions: List<Int>
  ): Deferred<List<S>> {
    val expectedIdsAndVersions = expectedVersions.map { expectedId to it }
    // Use the I/O dispatcher for blocking HTTP operations (since it's designed to handle blocking
    // operations that might otherwise stall a coroutine dispatcher).
    return CoroutineScope(dispatcher).async {
      val responseMap = resolveSync()
      val receivedIdsAndVersions =
        responseMap.mapTo(mutableSetOf()) { (id, structure) -> id to structure.version }
      val missingIds = expectedIdsAndVersions - receivedIdsAndVersions
      val extraIds = receivedIdsAndVersions - expectedIdsAndVersions.toSet()
      check(missingIds.isEmpty()) { "Missing ID/versions in response: $missingIds." }
      check(extraIds.isEmpty()) { "Received extra ID/versions in response: $missingIds." }

      // Return the structures in the order of the input IDs/versions map.
      val associatedMap = responseMap.entries.associate { (id, structure) ->
        (id to structure.version) to structure
      }
      return@async expectedIdsAndVersions.map { associatedMap.getValue(it) }
    }
  }

  private fun <S : VersionedStructure> Call<Map<String, S>>.resolveAsync(
    expectedId: String
  ): Deferred<S> {
    return CoroutineScope(dispatcher).async {
      val responses = resolveSync()
      checkNotNull(responses[expectedId]) {
        "Missing expected ID $expectedId from responses: $responses.".redact()
      }
    }
  }

  private suspend fun <S : VersionedStructure> Call<Map<String, S>>.resolveSync(): Map<String, S> {
    return withContext(Dispatchers.IO) {
      try {
        val result = execute()
        return@withContext if (result.isSuccessful) {
          checkNotNull(result.body()) {
            "Failed to receive body for request: ${request()}.".redact()
          }
        } else error("Failed to call: ${request()}. Encountered failure:\n$result.".redact())
      } catch (exception: Exception) {
        val metadata = RequestMetadata(request().method, request().url.toUrl().toExternalForm())
        val responseBodyText = memoizedRawResponses[metadata]
        throw IllegalStateException(
          "Failed to call: ${request()}. Response body:\n\n$responseBodyText".redact(), exception
        )
      }
    }
  }

  private fun String.redact(): String = replace(apiSecret, "<redacted_secret>")

  private inline fun <reified T : VersionedStructure> fetchLatestFromServiceAsync(
    type: String,
    id: String,
    crossinline fetch: (AndroidActivityRequests.Latest) -> Call<Map<String, T>>
  ): Deferred<T> {
    return CoroutineScope(dispatcher).async {
      if (forceCacheLoad && cacheDir != null) {
        // Try to load latest from the local directory, first.
        val expectedPrefix = computeFileNamePrefix(type, id, version = "")
        val mostRecentVersion = cacheDir.listFiles()?.filter {
          it.extension == "json" && it.nameWithoutExtension.startsWith(expectedPrefix)
        }?.maxOfOrNull { it.nameWithoutExtension.substringAfter(expectedPrefix).toInt() }
        if (mostRecentVersion != null) {
          return@async checkNotNull(tryLoadFromCache(type, NonLocalized(id, mostRecentVersion))) {
            "Something went wrong when trying to fetch latest $type from disk: $id."
          }
        }
      }

      fetch(AndroidActivityRequests.Latest(LatestVersion(id))).resolveAsync(id).await().also {
        maybeSaveToCache(type, NonLocalized(id, it.version), it)
      }
    }
  }

  private inline fun <
    reified T : VersionedStructure,
    R : ActivityRequest,
    RS : AndroidActivityRequests
    > fetchVersionedFromServiceAsync(
    type: String,
    id: String,
    versions: List<Int>,
    crossinline createRequest: (String, Int) -> R,
    crossinline createRequests: (List<R>) -> RS,
    crossinline fetch: (RS) -> Call<Map<String, T>>
  ): Deferred<List<T>> {
    require(versions.all { it >= 1 }) { "Versions must be >= 1." }
    require(versions.toSet().size == versions.size) { "Expected requested versions to be unique." }
    return CoroutineScope(dispatcher).async {
      val requests = versions.map { createRequest(id, it) }
      val localStructures = requests.map { tryLoadFromCache<T>(type, it) }
      val requestsRequiringRemoteFetching =
        localStructures.withIndex().filter { (_, structure) ->
          structure == null
        }.map { (index, _) -> index to requests[index] }
      val reqsCol = createRequests(requestsRequiringRemoteFetching.map { (_, req) -> req })
      val fetchResult = if (reqsCol.requests.isNotEmpty()) {
        // Only fetch if there are versions to retrieve.
        fetch(reqsCol).resolveAsyncVersionsAsync(id, versions).await()
      } else emptyList()
      val remoteStructures = fetchResult.withIndex().associate { (index, structure) ->
        requestsRequiringRemoteFetching[index].first to structure
      }
      // Merge locally and remotely fetched structures, then try to save everything to disk.
      return@async localStructures.mapIndexed { index, structure ->
        structure ?: remoteStructures.getValue(index)
      }.also { allStructures ->
        allStructures.forEachIndexed { index, structure ->
          maybeSaveToCache(type, requests[index], structure)
        }
      }
    }
  }

  private suspend inline fun <reified T : VersionedStructure> tryLoadFromCache(
    type: String, request: ActivityRequest
  ): T? {
    val expectedFilename = request.convertToFileName(type)
    val baseCacheDir = cacheDir ?: return null
    return withContext(Dispatchers.IO) {
      File(baseCacheDir, expectedFilename).takeIf(File::exists)?.let { file ->
        val buffer = Buffer().also { file.inputStream().use(it::readFrom) }
        checkNotNull(moshi.adapter(T::class.java).fromJson(buffer)) {
          "Failed to parse JSON file: ${file.path}."
        }
      }
    }
  }

  private suspend inline fun <reified T : VersionedStructure> maybeSaveToCache(
    type: String, request: ActivityRequest, structure: T
  ) {
    val expectedFilename = request.convertToFileName(type)
    val baseCacheDir = cacheDir ?: return
    withContext(Dispatchers.IO) {
      val expectedFile = File(baseCacheDir, expectedFilename)
      if (!expectedFile.exists()) {
        // Only write the saved file if it doesn't already exist, and if the structure successfully
        // converts to JSON.
        val buffer =
          Buffer().also { moshi.adapter(T::class.java).indent("  ").toJson(it, structure) }
        expectedFile.outputStream().use(buffer::writeTo)
      }
    }
  }

  private data class RequestMetadata(val method: String, val url: String)

  /**
   * Interceptor on top of Retrofit to modify requests and response.
   *
   * The interceptor removes the [XSSI_PREFIX] from every Oppia backend response to produce valid
   * JSON.
   */
  private class JsonPrefixRemoverNetworkInterceptor(
    private val memoizedRawResponses: MutableMap<RequestMetadata, String>
  ) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
      val request = chain.request()
      val originalResponse = chain.proceed(request)
      return originalResponse.newBuilder().apply {
        body(originalResponse.body?.stripXssiPrefix(request))
      }.build()
    }

    private fun ResponseBody.stripXssiPrefix(request: Request): ResponseBody {
      val textBody = string().removePrefix(XSSI_PREFIX).trimStart()
      val metadata = RequestMetadata(request.method, request.url.toUrl().toExternalForm())
      memoizedRawResponses[metadata] = textBody
      return textBody.toResponseBody(contentType())
    }

    private companion object {
      private const val XSSI_PREFIX = ")]}'"
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
  private class MoshiRequestsStringConverterFactory(moshi: Moshi) : Converter.Factory() {
    private val adapter by lazy { moshi.adapter(AndroidActivityRequests::class.java) }

    override fun stringConverter(
      type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<*, String>? {
      return if (AndroidActivityRequests::class.java.isAssignableFrom(type.rawType)) {
        Converter<Any, String> { adapter.toJson(it as AndroidActivityRequests) }
      } else null
    }
  }

  private companion object {
    private fun ActivityRequest.convertToFileName(type: String): String {
      return when (this) {
        is LatestVersion -> error("Cannot load/save latest versions of structures.")
        is NonLocalized -> "${computeFileNamePrefix(type, id, version.toString())}.json"
        is Localized ->
          "${computeFileNamePrefix(type, id, version.toString())}_lang-$languageCode.json"
      }
    }

    private fun computeFileNamePrefix(type: String, id: String, version: String): String =
      "${type}_id-${id}_ver-$version"
  }
}
