package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows

/**
 * Tests for [GooglePlayConsoleClient].
 *
 * Uses [MockWebServer] to intercept HTTP calls and verify request structure and response handling
 * without making real network calls to the Play Developer API.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GooglePlayConsoleClientTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var server: MockWebServer
  private lateinit var client: GooglePlayConsoleClient
  private val savedBaseUrl = GooglePlayConsoleClient.apiBaseUrl

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
    GooglePlayConsoleClient.apiBaseUrl = server.url("/").toString()
    client = GooglePlayConsoleClient(accessToken = "test-token")
  }

  @After
  fun tearDown() {
    server.shutdown()
    GooglePlayConsoleClient.apiBaseUrl = savedBaseUrl
  }

  // ---------------------------------------------------------------------------
  // createEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testCreateEdit_successResponse_returnsEditId() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-abc123"}""").setResponseCode(200))

    val editId = client.createEdit("org.oppia.android")

    assertThat(editId).isEqualTo("edit-abc123")
  }

  @Test
  fun testCreateEdit_sendsPostRequest() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    client.createEdit("org.oppia.android")

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).contains("org.oppia.android/edits")
  }

  @Test
  fun testCreateEdit_sendsAuthorizationHeader() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    client.createEdit("org.oppia.android")

    val request = server.takeRequest()
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token")
  }

  @Test
  fun testCreateEdit_errorResponse_throwsWithStatusCode() {
    server.enqueue(MockResponse().setResponseCode(403).setBody("Forbidden"))

    val exception = assertThrows<IllegalStateException>() {
      client.createEdit("org.oppia.android")
    }

    assertThat(exception).hasMessageThat().contains("403")
  }

  // ---------------------------------------------------------------------------
  // uploadAab
  // ---------------------------------------------------------------------------

  @Test
  fun testUploadAab_successResponse_returnsVersionCode() {
    // createEdit is called inside getTrackReleases; for uploadAab directly we need the edit ID.
    val aabFile = tempFolder.newFile("test.aab").also { it.writeBytes(ByteArray(32)) }
    server.enqueue(MockResponse().setBody("""{"versionCode":301}""").setResponseCode(200))

    val versionCode = client.uploadAab("org.oppia.android", "edit-1", aabFile.absolutePath)

    assertThat(versionCode).isEqualTo(301L)
  }

  @Test
  fun testUploadAab_sendsPostRequest() {
    val aabFile = tempFolder.newFile("test.aab").also { it.writeBytes(ByteArray(32)) }
    server.enqueue(MockResponse().setBody("""{"versionCode":301}""").setResponseCode(200))

    client.uploadAab("org.oppia.android", "edit-1", aabFile.absolutePath)

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).contains("bundles")
  }

  @Test
  fun testUploadAab_missingFile_throwsBeforeNetworkCall() {
    val exception = assertThrows<IllegalStateException>() {
      client.uploadAab("org.oppia.android", "edit-1", "/nonexistent/file.aab")
    }

    assertThat(exception).hasMessageThat().contains("AAB file not found")
    // No requests should have been sent.
    assertThat(server.requestCount).isEqualTo(0)
  }

  @Test
  fun testUploadAab_errorResponse_throwsWithDetails() {
    val aabFile = tempFolder.newFile("test.aab").also { it.writeBytes(ByteArray(32)) }
    server.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

    val exception = assertThrows<IllegalStateException>() {
      client.uploadAab("org.oppia.android", "edit-1", aabFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("400")
  }

  // ---------------------------------------------------------------------------
  // setTrackRelease
  // ---------------------------------------------------------------------------

  @Test
  fun testSetTrackRelease_successResponse_doesNotThrow() {
    server.enqueue(
      MockResponse()
        .setBody("""{"releases":[{"versionCodes":["301"],"status":"completed"}]}""")
        .setResponseCode(200)
    )

    // Should not throw.
    client.setTrackRelease(
      packageName = "org.oppia.android",
      editId = "edit-1",
      track = "alpha",
      versionCode = 301L,
      releaseNotes = mapOf("en-US" to "Bug fixes")
    )
  }

  @Test
  fun testSetTrackRelease_sendsPutRequest() {
    server.enqueue(
      MockResponse()
        .setBody("""{"releases":[]}""")
        .setResponseCode(200)
    )

    client.setTrackRelease("org.oppia.android", "edit-1", "alpha", 301L, emptyMap())

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("PUT")
    assertThat(request.path).contains("tracks/alpha")
  }

  @Test
  fun testSetTrackRelease_errorResponse_throwsWithDetails() {
    server.enqueue(MockResponse().setResponseCode(409).setBody("Conflict"))

    val exception = assertThrows<IllegalStateException>() {
      client.setTrackRelease("org.oppia.android", "edit-1", "alpha", 301L, emptyMap())
    }

    assertThat(exception).hasMessageThat().contains("409")
  }

  // ---------------------------------------------------------------------------
  // commitEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testCommitEdit_successResponse_doesNotThrow() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    // Should not throw.
    client.commitEdit("org.oppia.android", "edit-1")
  }

  @Test
  fun testCommitEdit_sendsPostRequest() {
    server.enqueue(MockResponse().setBody("""{"id":"edit-1"}""").setResponseCode(200))

    client.commitEdit("org.oppia.android", "edit-1")

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).contains("commit")
  }

  @Test
  fun testCommitEdit_errorResponse_throwsWithDetails() {
    server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

    val exception = assertThrows<IllegalStateException>() {
      client.commitEdit("org.oppia.android", "edit-1")
    }

    assertThat(exception).hasMessageThat().contains("500")
  }

  // ---------------------------------------------------------------------------
  // getTrackReleases
  // ---------------------------------------------------------------------------

  @Test
  fun testGetTrackReleases_parsesReleasesFromResponse() {
    // getTrackReleases internally calls createEdit first.
    server.enqueue(MockResponse().setBody("""{"id":"temp-edit"}""").setResponseCode(200))
    server.enqueue(
      MockResponse()
        .setBody(
          """{"releases":[{"versionCodes":["300"],"status":"completed"}]}"""
        )
        .setResponseCode(200)
    )

    val releases = client.getTrackReleases("org.oppia.android", "alpha")

    assertThat(releases).hasSize(1)
    assertThat(releases.first().status).isEqualTo("completed")
  }

  @Test
  fun testGetTrackReleases_nullReleasesField_returnsEmpty() {
    server.enqueue(MockResponse().setBody("""{"id":"temp-edit"}""").setResponseCode(200))
    server.enqueue(MockResponse().setBody("""{"releases":null}""").setResponseCode(200))

    val releases = client.getTrackReleases("org.oppia.android", "alpha")

    assertThat(releases).isEmpty()
  }

  @Test
  fun testGetTrackReleases_errorOnTrackGet_throwsWithDetails() {
    // createEdit succeeds, but getTrack fails.
    server.enqueue(MockResponse().setBody("""{"id":"temp-edit"}""").setResponseCode(200))
    server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

    val exception = assertThrows<IllegalStateException>() {
      client.getTrackReleases("org.oppia.android", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("404")
  }
}
