package org.oppia.android.scripts.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Production implementation of [VertexAiClient] that calls the Vertex AI REST API.
 *
 * Authentication is performed via a GCP Bearer token, typically obtained via Workload Identity
 * Federation inside a GitHub Actions workflow:
 * ```
 * gcloud auth print-access-token
 * ```
 *
 * Requests are sent synchronously to the Vertex AI `generateContent` endpoint for the configured
 * [gcpProject], [location], and [modelId].
 *
 * @property gcpProject GCP project ID that has Vertex AI enabled (e.g. "oppia-android-prod")
 * @property location Vertex AI region (e.g. "us-central1")
 * @property modelId Vertex AI model identifier (e.g. "gemini-1.5-flash")
 * @property gcpAccessToken GCP Bearer token for authenticating with the Vertex AI API
 */
class GoogleVertexAiClient(
  private val gcpProject: String,
  private val location: String,
  private val modelId: String,
  private val gcpAccessToken: String,
  private val overrideApiBaseUrl: String? = null
) : VertexAiClient {

  private val apiBaseUrl: String =
    overrideApiBaseUrl ?: "https://$location-aiplatform.googleapis.com"

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .callTimeout(90, TimeUnit.SECONDS)
    .build()
  private val moshi = Moshi.Builder().build()
  private val requestAdapter = moshi.adapter(GenerateContentRequest::class.java)
  private val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)

  override fun generateText(prompt: String): String {
    val endpoint = buildEndpointUrl()
    val requestBody = GenerateContentRequest(
      contents = listOf(
        Content(parts = listOf(Part(text = prompt)))
      )
    )
    val jsonBody = checkNotNull(requestAdapter.toJson(requestBody)) {
      "Failed to serialize Vertex AI request body."
    }

    val request = Request.Builder()
      .url(endpoint)
      .addHeader("Authorization", "Bearer $gcpAccessToken")
      .addHeader("Content-Type", "application/json")
      .method("POST", jsonBody.toRequestBody(JSON_MEDIA_TYPE))
      .build()

    val responseBody = httpClient.newCall(request).execute().use { response ->
      val body = response.body?.string()
      check(response.isSuccessful) {
        "Vertex AI API call failed with HTTP ${response.code}: $body"
      }
      checkNotNull(body) {
        "Vertex AI returned an empty response body."
      }
    }

    val parsed = checkNotNull(responseAdapter.fromJson(responseBody)) {
      "Failed to parse Vertex AI response: $responseBody"
    }
    return checkNotNull(
      parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    ) {
      "Vertex AI response contained no text candidates: $responseBody"
    }.trim()
  }

  private fun buildEndpointUrl(): String {
    return "$apiBaseUrl/v1/projects/$gcpProject/locations/$location" +
      "/publishers/google/models/$modelId:generateContent"
  }

  /**
   * Top-level request body sent to the Vertex AI generateContent endpoint.
   *
   * @property contents the list of content turns to send to the model
   */
  @JsonClass(generateAdapter = true)
  data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>
  )

  /**
   * Represents a single content turn containing one or more parts.
   *
   * @property parts the list of content parts in this turn
   */
  @JsonClass(generateAdapter = true)
  data class Content(
    @Json(name = "parts") val parts: List<Part>
  )

  /**
   * A single text part within a [Content] turn.
   *
   * @property text the text content of this part
   */
  @JsonClass(generateAdapter = true)
  data class Part(
    @Json(name = "text") val text: String
  )

  /**
   * Top-level response from the Vertex AI generateContent endpoint.
   *
   * @property candidates the list of generated response candidates, or null if none were returned
   */
  @JsonClass(generateAdapter = true)
  data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
  )

  /**
   * A single candidate response from the model.
   *
   * @property content the content of this candidate, or null if the model returned no content
   */
  @JsonClass(generateAdapter = true)
  data class Candidate(
    @Json(name = "content") val content: Content?
  )

  companion object {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
  }
}
