package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.LatestVersion
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.Localized
import org.oppia.android.scripts.gae.json.AndroidActivityRequests.ActivityRequest.NonLocalized
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

private typealias ActReq = ActivityRequest
private typealias AndroidActReqs = AndroidActivityRequests
private typealias VersionedStructures<S> = List<VersionedStructure<S>>

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

  fun fetchLatestClassroomAsync(name: String): Deferred<VersionedStructure<GaeClassroom>> {
    return fetchLatestFromServiceAsync(
      type = "classroom",
      id = name,
      fetch = apiService::fetchLatestClassroom,
      retrieveStructureVersion = null // Classroom versions aren't exposed in the API.
    )
  }

  fun fetchLatestExplorationAsync(id: String): Deferred<VersionedStructure<GaeExploration>> {
    return fetchLatestFromServiceAsync(
      type = "exploration",
      id = id,
      fetch = apiService::fetchLatestExploration,
      retrieveStructureVersion = GaeExploration::version
    )
  }

  fun fetchExplorationByVersionsAsync(
    id: String,
    versions: List<Int>
  ): Deferred<List<VersionedStructure<GaeExploration>>> {
    return fetchVersionedFromServiceAsync(
      type = "exploration",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchExplorationByVersion,
      retrieveStructureVersion = GaeExploration::version
    )
  }

  fun fetchLatestStoryAsync(id: String): Deferred<VersionedStructure<GaeStory>> {
    return fetchLatestFromServiceAsync(
      type = "story",
      id = id,
      fetch = apiService::fetchLatestStory,
      retrieveStructureVersion = GaeStory::version
    )
  }

  fun fetchStoryByVersionsAsync(
    id: String,
    versions: List<Int>
  ): Deferred<List<VersionedStructure<GaeStory>>> {
    return fetchVersionedFromServiceAsync(
      type = "story",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchStoryByVersion,
      retrieveStructureVersion = GaeStory::version
    )
  }

  fun fetchLatestConceptCardAsync(skillId: String): Deferred<VersionedStructure<GaeSkill>> {
    return fetchLatestFromServiceAsync(
      type = "concept_card",
      id = skillId,
      fetch = apiService::fetchLatestConceptCard,
      retrieveStructureVersion = GaeSkill::version
    )
  }

  fun fetchConceptCardByVersionsAsync(
    skillId: String,
    versions: List<Int>
  ): Deferred<List<VersionedStructure<GaeSkill>>> {
    return fetchVersionedFromServiceAsync(
      type = "concept_card",
      id = skillId,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchConceptCardByVersion,
      retrieveStructureVersion = GaeSkill::version
    )
  }

  fun fetchLatestRevisionCardAsync(
    topicId: String,
    subtopicIndex: Int
  ): Deferred<VersionedStructure<GaeSubtopicPage>> {
    return fetchLatestFromServiceAsync(
      type = "revision_card",
      id = "$topicId-$subtopicIndex",
      fetch = apiService::fetchLatestRevisionCard,
      retrieveStructureVersion = GaeSubtopicPage::version
    )
  }

  fun fetchRevisionCardByVersionsAsync(
    topicId: String,
    subtopicIndex: Int,
    versions: List<Int>
  ): Deferred<List<VersionedStructure<GaeSubtopicPage>>> {
    return fetchVersionedFromServiceAsync(
      type = "revision_card",
      id = "$topicId-$subtopicIndex",
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchRevisionCardByVersion,
      retrieveStructureVersion = GaeSubtopicPage::version
    )
  }

  fun fetchLatestTopicAsync(id: String): Deferred<VersionedStructure<GaeTopic>> {
    return fetchLatestFromServiceAsync(
      type = "topic",
      id = id,
      fetch = apiService::fetchLatestTopic,
      retrieveStructureVersion = GaeTopic::version
    )
  }

  fun fetchTopicByVersionsAsync(
    id: String,
    versions: List<Int>
  ): Deferred<List<VersionedStructure<GaeTopic>>> {
    return fetchVersionedFromServiceAsync(
      type = "topic",
      id = id,
      versions = versions,
      createRequest = ::NonLocalized,
      createRequests = AndroidActivityRequests::NonLocalized,
      fetch = apiService::fetchTopicByVersion,
      retrieveStructureVersion = GaeTopic::version
    )
  }

  fun fetchExplorationTranslationsAsync(
    explorationId: String,
    explorationVersion: Int,
    languageCode: String
  ): Deferred<VersionedStructure<GaeEntityTranslations>> {
    val fullFetch = fetchVersionedFromServiceAsync(
      type = "exploration_translations",
      id = explorationId,
      versions = listOf(explorationVersion),
      createRequest = { id, version -> Localized(id, version, languageCode) },
      createRequests = AndroidActivityRequests::Localized,
      fetch = apiService::fetchExplorationTranslations,
      retrieveStructureVersion = null // There's no version passed with translations.
    )
    return CoroutineScope(dispatcher).async { fullFetch.await().single() }
  }

  private fun <S> Call<VersionedStructures<S>>.resolveAsyncVersionsAsync(
    expectedId: String,
    expectedVersions: List<Int>
  ): Deferred<VersionedStructures<S>> {
    val expectedIdsAndVersions = expectedVersions.map { expectedId to it }
    // Use the I/O dispatcher for blocking HTTP operations (since it's designed to handle blocking
    // operations that might otherwise stall a coroutine dispatcher).
    return CoroutineScope(dispatcher).async {
      val responses = resolveSync()
      val receivedIdsAndVersions =
        responses.mapTo(mutableSetOf()) { versioned -> versioned.id to versioned.version }
      val missingIds = expectedIdsAndVersions - receivedIdsAndVersions
      val extraIds = receivedIdsAndVersions - expectedIdsAndVersions.toSet()
      check(missingIds.isEmpty()) {
        "Missing ID/versions in response: $missingIds. Received: $receivedIdsAndVersions."
      }
      check(extraIds.isEmpty()) {
        "Received extra ID/versions in response: $missingIds. Received: $receivedIdsAndVersions."
      }

      // Return the structures in the order of the input IDs/versions map.
      val associatedMap = responses.associateBy { versioned -> versioned.id to versioned.version }
      return@async expectedIdsAndVersions.map { associatedMap.getValue(it) }
    }
  }

  private fun <S> Call<VersionedStructures<S>>.resolveAsync(
    expectedId: String
  ): Deferred<VersionedStructure<S>> {
    return CoroutineScope(dispatcher).async {
      val responses = resolveSync()
      checkNotNull(responses.singleOrNull { it.id == expectedId }) {
        "Missing expected ID $expectedId from responses: $responses.".redact()
      }
    }
  }

  private suspend fun <S> Call<VersionedStructures<S>>.resolveSync(): VersionedStructures<S> {
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

  private inline fun <reified T> fetchLatestFromServiceAsync(
    type: String,
    id: String,
    crossinline fetch: (AndroidActivityRequests.Latest) -> Call<List<VersionedStructure<T>>>,
    noinline retrieveStructureVersion: ((T) -> Int)?
  ): Deferred<VersionedStructure<T>> {
    return CoroutineScope(dispatcher).async {
      if (forceCacheLoad && cacheDir != null) {
        // Try to load latest from the local directory, first.
        val expectedPrefix = computeFileNamePrefix(type, id, version = "")
        val mostRecentVersion = cacheDir.listFiles()?.filter {
          it.extension == "json" && it.nameWithoutExtension.startsWith(expectedPrefix)
        }?.mapNotNull { file ->
          // Files with "latest" 'versions' should always be refetched unless there's an explicitly
          // cached version on disk.
          file.nameWithoutExtension.substringAfter(expectedPrefix).takeIf { it != "latest" }
        }?.maxOfOrNull { it.toInt() }
        if (mostRecentVersion != null) {
          return@async checkNotNull(tryLoadFromCache(type, NonLocalized(id, mostRecentVersion))) {
            "Something went wrong when trying to fetch latest $type from disk: $id."
          }
        }
      }

      val request = AndroidActivityRequests.Latest(LatestVersion(id))
      val remoteStructure = fetch(request).resolveAsync(id).await()
      // Ensure that the returned structure has the correct version (if it's known).
      return@async if (retrieveStructureVersion != null) {
        remoteStructure.copy(version = retrieveStructureVersion(remoteStructure.payload)).also {
          maybeSaveToCache(type, NonLocalized(id, it.expectedVersion), it)
        }
      } else remoteStructure.also { maybeSaveToCache(type, LatestVersion(id), it) }
    }
  }

  private inline fun <reified T, R : ActReq, RS : AndroidActReqs> fetchVersionedFromServiceAsync(
    type: String,
    id: String,
    versions: List<Int>,
    crossinline createRequest: (String, Int) -> R,
    crossinline createRequests: (List<R>) -> RS,
    crossinline fetch: (RS) -> Call<List<VersionedStructure<T>>>,
    noinline retrieveStructureVersion: ((T) -> Int)?
  ): Deferred<List<VersionedStructure<T>>> {
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
        fetch(reqsCol).resolveAsyncVersionsAsync(id, versions).await().map { structure ->
          // Ensure that the returned structures have the correct remote versions (since the web
          // controller isn't consistent in when it provides a version).
          if (retrieveStructureVersion != null) {
            structure.copy(version = retrieveStructureVersion(structure.payload))
          } else structure
        }
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

  private suspend inline fun <reified T> tryLoadFromCache(
    type: String,
    request: ActivityRequest
  ): VersionedStructure<T>? {
    val expectedFilename = request.convertToFileName(type)
    val baseCacheDir = cacheDir ?: return null
    return withContext(Dispatchers.IO) {
      File(baseCacheDir, expectedFilename).takeIf(File::exists)?.let { file ->
        val buffer = Buffer().also { file.inputStream().use(it::readFrom) }
        val activityType = Types.newParameterizedType(VersionedStructure::class.java, T::class.java)
        checkNotNull(moshi.adapter<VersionedStructure<T>>(activityType).fromJson(buffer)) {
          "Failed to parse JSON file: ${file.path}."
        }
      }
    }
  }

  // TODO: Update caching to ensure all versions are cached along with their analysis results (as part of the repository creation?). This can provide substantially more debugging insight when something goes wrong.
  private suspend inline fun <reified T> maybeSaveToCache(
    type: String,
    request: ActivityRequest,
    structure: VersionedStructure<T>
  ) {
    val expectedFilename = request.convertToFileName(type)
    val baseCacheDir = cacheDir ?: return
    withContext(Dispatchers.IO) {
      val expectedFile = File(baseCacheDir, expectedFilename)
      if (!expectedFile.exists()) {
        // Only write the saved file if it doesn't already exist, and if the structure successfully
        // converts to JSON.
        val buffer = Buffer().also {
          moshi.adapter<VersionedStructure<T>>(
            Types.newParameterizedType(VersionedStructure::class.java, T::class.java)
          ).indent("  ").toJson(it, structure)
        }
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
      type: Type,
      annotations: Array<out Annotation>,
      retrofit: Retrofit
    ): Converter<*, String>? {
      return if (AndroidActivityRequests::class.java.isAssignableFrom(type.rawType)) {
        Converter<Any, String> { adapter.toJson(it as AndroidActivityRequests) }
      } else null
    }
  }

  private companion object {
    private fun ActivityRequest.convertToFileName(type: String): String {
      return when (this) {
        is LatestVersion -> "${computeFileNamePrefix(type, id, "latest")}.json"
        is NonLocalized -> "${computeFileNamePrefix(type, id, version.toString())}.json"
        is Localized ->
          "${computeFileNamePrefix(type, id, version.toString())}_lang-$languageCode.json"
      }
    }

    private fun computeFileNamePrefix(type: String, id: String, version: String): String =
      "${type}_id-${id}_ver-$version"
  }
}
