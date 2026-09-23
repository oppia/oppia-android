package org.oppia.android.scripts.release

/**
 * Client for invoking Vertex AI Generative Language models (e.g., Gemini) to produce
 * user-facing text summaries.
 *
 * Implementations are expected to handle HTTP communication and authentication
 * (typically via a GCP access token obtained through Workload Identity Federation).
 * The interface is intentionally narrow so that tests can inject a [FakeVertexAiClient]
 * without needing a real GCP project.
 */
interface VertexAiClient {
  /**
   * Sends [prompt] to the configured Vertex AI model and returns the generated text.
   *
   * The returned string is the raw text part of the first candidate response. Callers are
   * responsible for trimming or post-processing the output as needed.
   *
   * @param prompt the full prompt to send to the model
   * @return the generated text from the model's first candidate response
   * @throws Exception if the HTTP call fails, the model returns a non-OK status, or the
   *     response cannot be parsed
   */
  fun generateText(prompt: String): String
}
