package org.oppia.android.scripts.release.remote

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.oppia.android.scripts.release.model.InsertEditRequest
import org.oppia.android.scripts.release.model.TrackUpdateRequest
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Tests for [PlayConsoleService].
 *
 * Verifies that each service method sends the correct HTTP verb and URL path to the server.
 * Uses [MockWebServer] so no real network calls are made.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class PlayConsoleServiceTest {
  private lateinit var server: MockWebServer
  private lateinit var service: PlayConsoleService

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
    service = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(
        MoshiConverterFactory.create(
          Moshi.Builder()
            .add(InsertEditRequest::class.java, InsertEditRequest.ADAPTER)
            .build()
        )
      )
      .client(OkHttpClient())
      .build()
      .create(PlayConsoleService::class.java)
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  // ---------------------------------------------------------------------------
  // insertEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testInsertEdit_sendsPostToCorrectPath() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    service.insertEdit("org.oppia.android", "Bearer token").execute()

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).isEqualTo("/org.oppia.android/edits")
  }

  @Test
  fun testInsertEdit_sendsAuthorizationHeader() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    service.insertEdit("org.oppia.android", "Bearer my-token").execute()

    val request = server.takeRequest()
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer my-token")
  }

  // ---------------------------------------------------------------------------
  // uploadBundle
  // ---------------------------------------------------------------------------

  @Test
  fun testUploadBundle_sendsPostToCorrectPath() {
    server.enqueue(MockResponse().setBody("""{"versionCode":301}""").setResponseCode(200))
    val body = ByteArray(8).toRequestBody()

    service.uploadBundle("org.oppia.android", "edit-1", "Bearer token", body).execute()

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).contains("org.oppia.android/edits/edit-1/bundles")
  }

  // ---------------------------------------------------------------------------
  // updateTrack
  // ---------------------------------------------------------------------------

  @Test
  fun testUpdateTrack_sendsPutToCorrectPath() {
    server.enqueue(MockResponse().setBody("""{"releases":[]}""").setResponseCode(200))
    val body = TrackUpdateRequest(track = "alpha", releases = emptyList())

    service.updateTrack("org.oppia.android", "edit-1", "alpha", "Bearer token", body).execute()

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("PUT")
    assertThat(request.path).contains("org.oppia.android/edits/edit-1/tracks/alpha")
  }

  // ---------------------------------------------------------------------------
  // commitEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testCommitEdit_sendsPostToCorrectPath() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    service.commitEdit("org.oppia.android", "edit-1", "Bearer token").execute()

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).contains("org.oppia.android/edits/edit-1:commit")
  }

  // ---------------------------------------------------------------------------
  // getTrack
  // ---------------------------------------------------------------------------

  @Test
  fun testGetTrack_sendsGetToCorrectPath() {
    server.enqueue(MockResponse().setBody("""{"releases":[]}""").setResponseCode(200))

    service.getTrack("org.oppia.android", "edit-1", "alpha", "Bearer token").execute()

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("GET")
    assertThat(request.path).contains("org.oppia.android/edits/edit-1/tracks/alpha")
  }
}
