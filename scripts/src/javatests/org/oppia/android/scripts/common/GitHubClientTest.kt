package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.testing.assertThrows
import java.lang.IllegalStateException

/** Tests for [GitHubClient]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GitHubClientTest {
  private companion object {
    private const val TEST_AUTH_TOKEN = "abcdef1234567890"
  }

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }
  private lateinit var mockWebServer: MockWebServer

  @Before
  fun setUp() {
    mockWebServer = MockWebServer()
    GitHubClient.remoteApiUrl = mockWebServer.url("/").toString()
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testFetchAllOpenIssuesAsync_noGhTool_throwsException() {
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }
    }

    assertThat(exception).hasMessageThat().contains("Failed to interact with gh tool.")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_ghTool_missingAuthToken_throwsException() {
    setUpSupportForGhAuthWithMissingToken()
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }
    }

    assertThat(exception).hasMessageThat().contains("Failed to retrieve auth token from GH tool.")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withInvalidAuthToken_throwsException() {
    setUpSupportForGhAuth(authToken = "Invalid")
    setGitHubServiceNextResponseWithFailureCode(errorCode = 401) // Simulate invalid auth token.
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }
    }

    assertThat(exception).hasMessageThat().contains("Failed to fetch issues at page")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_requestHasCorrectPath() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    val request = mockWebServer.takeRequest()
    assertThat(request.requestUrl?.encodedPath).isEqualTo("/repos/oppia/oppia-android/issues")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_requestIncludesAuthorizationBearer() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    val request = mockWebServer.takeRequest()
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer $TEST_AUTH_TOKEN")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_requestIncludesDataFormat() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    val request = mockWebServer.takeRequest()
    assertThat(request.getHeader("Accept")).isEqualTo("application/vnd.github+json")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_requestIncludesApiVersion() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    val request = mockWebServer.takeRequest()
    assertThat(request.getHeader("X-GitHub-Api-Version")).isEqualTo("2022-11-28")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_requestsQueriesIssuesInAscendingOrder() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    val request = mockWebServer.takeRequest()
    assertThat(request.requestUrl?.queryParameter("direction")).isEqualTo("asc")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_missingResourceResponse_throwsException() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithFailureCode(errorCode = 404)
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }
    }

    assertThat(exception).hasMessageThat().contains("Failed to fetch issues at page")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_nullBody_throwsException() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNullResponse()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val exception = assertThrows<IllegalStateException>() {
      runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }
    }

    assertThat(exception).hasMessageThat().contains("No issues response from GitHub for page")
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_noIssues_returnEmptyList() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithNoIssues()
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val openIssues = runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    assertThat(openIssues).isEmpty()
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_someIssues_returnIssuesList() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithSinglePageOfIssues(11, 57)
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val openIssues = runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    assertThat(openIssues).hasSize(2)
    assertThat(openIssues[0].number).isEqualTo(11)
    assertThat(openIssues[1].number).isEqualTo(57)
  }

  @Test
  fun testFetchAllOpenIssuesAsync_withAuthToken_multiplePagesOfIssues_returnsAllIssues() {
    setUpSupportForGhAuth(authToken = TEST_AUTH_TOKEN)
    setGitHubServiceNextResponseWithPagesOfIssues(listOf(11, 57), listOf(42), listOf(77, 28))
    val gitHubClient = GitHubClient(tempFolder.root, scriptBgDispatcher, fakeCommandExecutor)

    val openIssues = runBlocking { gitHubClient.fetchAllOpenIssuesAsync().await() }

    assertThat(openIssues).hasSize(5)
    assertThat(openIssues[0].number).isEqualTo(11)
    assertThat(openIssues[1].number).isEqualTo(57)
    assertThat(openIssues[2].number).isEqualTo(42)
    assertThat(openIssues[3].number).isEqualTo(77)
    assertThat(openIssues[4].number).isEqualTo(28)
  }

  private fun setUpSupportForGhAuthWithMissingToken() {
    fakeCommandExecutor.registerHandler("gh") { _, args, _, errorStream ->
      when (args) {
        listOf("help") -> 0
        listOf("auth", "token") -> 1.also { errorStream.println("No auth token configured.") }
        else -> 1
      }
    }
  }

  private fun setUpSupportForGhAuth(authToken: String) {
    fakeCommandExecutor.registerHandler("gh") { _, args, outputStream, _ ->
      when (args) {
        listOf("help") -> 0
        listOf("auth", "token") -> 0.also { outputStream.print(authToken) }
        else -> 1
      }
    }
  }

  private fun setGitHubServiceNextResponseWithNoIssues() {
    setGitHubServiceNextResponseWithSinglePageOfIssues(/* no issues */)
  }

  private fun setGitHubServiceNextResponseWithSinglePageOfIssues(vararg issueNumbers: Int) {
    setGitHubServiceNextResponseWithPagesOfIssues(issueNumbers.toList())
  }

  private fun setGitHubServiceNextResponseWithPagesOfIssues(vararg issuePages: List<Int>) {
    issuePages.forEach { issueNumbers ->
      val issueJsons = issueNumbers.joinToString(separator = ",") { "{\"number\":$it}" }
      setGitHubServiceNextResponseWithJsonResponse("[$issueJsons]")
    }
    setGitHubServiceNextResponseWithJsonResponse("[]") // No more issues.
  }

  private fun setGitHubServiceNextResponseWithNullResponse() {
    setGitHubServiceNextResponseWithJsonResponse(rawJson = "null")
  }

  private fun setGitHubServiceNextResponseWithJsonResponse(rawJson: String) {
    mockWebServer.enqueue(MockResponse().setBody(rawJson))
  }

  private fun setGitHubServiceNextResponseWithFailureCode(errorCode: Int) {
    mockWebServer.enqueue(MockResponse().setResponseCode(errorCode))
  }
}
