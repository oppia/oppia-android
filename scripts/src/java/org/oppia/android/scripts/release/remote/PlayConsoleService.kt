package org.oppia.android.scripts.release.remote

import okhttp3.RequestBody
import org.oppia.android.scripts.release.model.BundleResponse
import org.oppia.android.scripts.release.model.EditResponse
import org.oppia.android.scripts.release.model.InsertEditRequest
import org.oppia.android.scripts.release.model.TrackResponse
import org.oppia.android.scripts.release.model.TrackUpdateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit service interface for the Google Play Developer Publishing API v3.
 *
 * API reference: https://developers.google.com/android-publisher/api-ref/rest
 *
 * All methods operate on a specific [packageName] (e.g. "org.oppia.android"). Write operations
 * require an active edit session (identified by [editId]) created via [insertEdit].
 */
interface PlayConsoleService {

  /**
   * Creates a new edit session for the given package.
   *
   * API reference: https://developers.google.com/android-publisher/api-ref/rest/v3/edits/insert
   *
   * @param packageName the application package name
   * @param authorizationBearer the OAuth2 bearer token for authentication
   * @return the [EditResponse] containing the new edit session ID
   */
  @POST("androidpublisher/v3/applications/{packageName}/edits")
  fun insertEdit(
    @Path("packageName") packageName: String,
    @Header("Authorization") authorizationBearer: String,
    @Body body: InsertEditRequest = InsertEditRequest()
  ): Call<EditResponse>

  /**
   * Uploads an Android App Bundle (AAB) to an active edit session.
   *
   * The AAB binary must be sent as `application/octet-stream`. The Play Console assigns a version
   * code to the uploaded bundle upon success.
   *
   * API reference:
   * https://developers.google.com/android-publisher/api-ref/rest/v3/edits.bundles/upload
   *
   * @param packageName the application package name
   * @param editId the active edit session ID
   * @param authorizationBearer the OAuth2 bearer token for authentication
   * @param aabBody the AAB file content as a raw [RequestBody]
   * @return the [BundleResponse] containing the assigned version code
   */
  @POST("upload/androidpublisher/v3/applications/{packageName}/edits/{editId}/bundles?uploadType=media")
  fun uploadBundle(
    @Path("packageName") packageName: String,
    @Path("editId") editId: String,
    @Header("Authorization") authorizationBearer: String,
    @Body aabBody: RequestBody
  ): Call<BundleResponse>

  /**
   * Updates a release track within an active edit session.
   *
   * Assigns the uploaded version code to the specified track with release metadata including
   * release notes.
   *
   * API reference:
   * https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks/update
   *
   * @param packageName the application package name
   * @param editId the active edit session ID
   * @param track the Play Console track (e.g. "alpha", "beta", "production")
   * @param authorizationBearer the OAuth2 bearer token for authentication
   * @param trackBody the track update configuration
   * @return the [TrackResponse] reflecting the updated track state
   */
  @PUT("androidpublisher/v3/applications/{packageName}/edits/{editId}/tracks/{track}")
  fun updateTrack(
    @Path("packageName") packageName: String,
    @Path("editId") editId: String,
    @Path("track") track: String,
    @Header("Authorization") authorizationBearer: String,
    @Body trackBody: TrackUpdateRequest
  ): Call<TrackResponse>

  /**
   * Commits an edit session, publishing all pending changes to the Play Console for review.
   *
   * This operation is irreversible — once committed, uploaded bundles and track assignments cannot
   * be rolled back via the API. Changes require a final human approval in the Play Console
   * before they go live.
   *
   * API reference: https://developers.google.com/android-publisher/api-ref/rest/v3/edits/commit
   *
   * @param packageName the application package name
   * @param editId the active edit session ID to commit
   * @param authorizationBearer the OAuth2 bearer token for authentication
   * @return the [EditResponse] confirming the committed edit
   */
  @POST("androidpublisher/v3/applications/{packageName}/edits/{editId}:commit")
  fun commitEdit(
    @Path("packageName") packageName: String,
    @Path("editId") editId: String,
    @Header("Authorization") authorizationBearer: String
  ): Call<EditResponse>

  /**
   * Retrieves the current track configuration, including all active releases.
   *
   * API reference: https://developers.google.com/android-publisher/api-ref/rest/v3/edits.tracks/get
   *
   * @param packageName the application package name
   * @param editId the edit session ID (required by the API path even for read operations)
   * @param track the Play Console track to query
   * @param authorizationBearer the OAuth2 bearer token for authentication
   * @return the [TrackResponse] containing the current releases on the track
   */
  @GET("androidpublisher/v3/applications/{packageName}/edits/{editId}/tracks/{track}")
  fun getTrack(
    @Path("packageName") packageName: String,
    @Path("editId") editId: String,
    @Path("track") track: String,
    @Header("Authorization") authorizationBearer: String
  ): Call<TrackResponse>
}
