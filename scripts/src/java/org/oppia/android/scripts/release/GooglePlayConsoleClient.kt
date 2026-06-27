package org.oppia.android.scripts.release

import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.oppia.android.scripts.release.model.InsertEditRequest
import org.oppia.android.scripts.release.model.TrackResponse
import org.oppia.android.scripts.release.model.TrackUpdateRequest
import org.oppia.android.scripts.release.remote.PlayConsoleService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Production implementation of [PlayConsoleClient] that communicates with the Google Play Developer
 * Publishing API v3 via Retrofit.
 *
 * This client uses an OAuth2 bearer token for authentication. In CI, this token is typically
 * obtained via Workload Identity Federation (WIF) from the GitHub Actions OIDC provider. The token
 * must have the `https://www.googleapis.com/auth/androidpublisher` scope.
 *
 * All write operations follow the edit session workflow:
 * 1. [createEdit] — opens a new edit session
 * 2. [uploadAab] / [setTrackRelease] — performs changes within the session
 * 3. [commitEdit] — publishes all changes atomically
 *
 * @property accessToken the OAuth2 bearer token for API authentication
 * @property apiBaseUrl the base URL for the Play Developer Publishing API; defaults to the
 *     production endpoint and can be overridden in tests to point at a local mock server
 * @property connectTimeoutMs the HTTP connection timeout in milliseconds; defaults to 120 seconds
 *     to accommodate large AAB uploads
 */
class GooglePlayConsoleClient(
  private val accessToken: String,
  private val apiBaseUrl: String = PRODUCTION_API_BASE_URL,
  private val connectTimeoutMs: Long = DEFAULT_TIMEOUT_MS
) : PlayConsoleClient {

  private val authorizationBearer by lazy { "Bearer $accessToken" }

  private val okHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
      .readTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
      .writeTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
      .build()
  }

  private val moshi by lazy { Moshi.Builder().build() }

  private val retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(apiBaseUrl)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .client(okHttpClient)
      .build()
  }

  private val playConsoleService by lazy { retrofit.create(PlayConsoleService::class.java) }

  override fun createEdit(packageName: String): String {
    val response = playConsoleService
      .insertEdit(packageName, authorizationBearer)
      .execute()
    check(response.isSuccessful) {
      "Failed to create edit for '$packageName': ${response.code()} ${response.message()}" +
        "\n${response.errorBody()?.string()}"
    }
    return checkNotNull(response.body()) {
      "Edit creation returned null body for '$packageName'."
    }.id
  }

  override fun getTrackReleases(
    packageName: String,
    track: String
  ): List<PlayConsoleClient.TrackRelease> {
    // A temporary edit is needed to read track info via the API.
    val editId = createEdit(packageName)
    val response = playConsoleService
      .getTrack(packageName, editId, track, authorizationBearer)
      .execute()
    check(response.isSuccessful) {
      "Failed to get track '$track' for '$packageName': ${response.code()} ${response.message()}" +
        "\n${response.errorBody()?.string()}"
    }
    val trackResponse = checkNotNull(response.body()) {
      "Track response returned null body for '$packageName' track '$track'."
    }
    // Sort by version code descending; Play Console does not guarantee ordering on releases.
    return trackResponse.releases
      ?.map { it.toTrackRelease() }
      ?.sortedByDescending { it.versionCodes.maxOrNull() }
      ?: emptyList()
  }

  override fun uploadAab(packageName: String, editId: String, aabPath: String): Long {
    val aabFile = File(aabPath)
    check(aabFile.exists()) { "AAB file not found at path: $aabPath" }
    val requestBody = aabFile.asRequestBody(OCTET_STREAM_MEDIA_TYPE)
    val response = playConsoleService
      .uploadBundle(packageName, editId, authorizationBearer, requestBody)
      .execute()
    check(response.isSuccessful) {
      "Failed to upload AAB for '$packageName' (edit=$editId): " +
        "${response.code()} ${response.message()}\n${response.errorBody()?.string()}"
    }
    return checkNotNull(response.body()) {
      "Bundle upload returned null body for '$packageName' (edit=$editId)."
    }.versionCode.toLong()
  }

  override fun setTrackRelease(
    packageName: String,
    editId: String,
    track: String,
    versionCode: Long,
    rolloutFraction: Int,
    releaseNotes: Map<String, String>
  ) {
    val fraction = rolloutFraction / 1000.0
    val status = if (rolloutFraction >= 1000) "completed" else "inProgress"
    val trackUpdate = TrackUpdateRequest(
      track = track,
      releases = listOf(
        TrackUpdateRequest.ReleaseEntry(
          versionCodes = listOf(versionCode.toString()),
          status = status,
          releaseNotes = releaseNotes.map { (lang, text) ->
            TrackUpdateRequest.LocalizedText(language = lang, text = text)
          },
          userFraction = if (rolloutFraction < 1000) fraction else null
        )
      )
    )
    val response = playConsoleService
      .updateTrack(packageName, editId, track, authorizationBearer, trackUpdate)
      .execute()
    check(response.isSuccessful) {
      "Failed to set track '$track' for '$packageName' (edit=$editId, vc=$versionCode): " +
        "${response.code()} ${response.message()}\n${response.errorBody()?.string()}"
    }
  }

  override fun commitEdit(packageName: String, editId: String) {
    val response = playConsoleService
      .commitEdit(packageName, editId, authorizationBearer)
      .execute()
    check(response.isSuccessful) {
      "Failed to commit edit '$editId' for '$packageName': " +
        "${response.code()} ${response.message()}\n${response.errorBody()?.string()}"
    }
  }

  companion object {
    /** Default HTTP timeout of 2 minutes to accommodate large AAB uploads (~150 MB). */
    private const val DEFAULT_TIMEOUT_MS = 120_000L

    private val OCTET_STREAM_MEDIA_TYPE = "application/octet-stream".toMediaType()

    /** Base URL for the Google Play Developer Publishing API v3 production endpoint. */
    const val PRODUCTION_API_BASE_URL =
      "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/"
  }

  private fun TrackResponse.ReleaseEntry.toTrackRelease(): PlayConsoleClient.TrackRelease {
    return PlayConsoleClient.TrackRelease(
      versionCodes = versionCodes?.map { it.toLong() } ?: emptyList(),
      status = status
    )
  }
}
