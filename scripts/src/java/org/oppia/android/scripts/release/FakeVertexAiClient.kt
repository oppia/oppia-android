package org.oppia.android.scripts.release

/**
 * In-memory fake implementation of [VertexAiClient] for use in unit tests.
 *
 * By default, [generateText] returns [defaultResponse]. Tests can simulate an LLM failure by
 * setting [shouldFail] to `true`, which causes the next call to throw an [IllegalStateException].
 * The flag resets to `false` after each thrown exception so that subsequent calls succeed again
 * (unless re-set). All prompts received are recorded in [receivedPrompts] for assertion.
 *
 * @property defaultResponse the text returned by [generateText] when [shouldFail] is false
 */
class FakeVertexAiClient(
  private val defaultResponse: String = "Fake generated changelog summary."
) : VertexAiClient {

  /** Whether the next call to [generateText] should throw to simulate an LLM failure. */
  var shouldFail = false

  /** All prompts passed to [generateText], in call order. */
  val receivedPrompts = mutableListOf<String>()

  override fun generateText(prompt: String): String {
    receivedPrompts += prompt
    if (shouldFail) {
      shouldFail = false
      error("FakeVertexAiClient: simulated Vertex AI failure")
    }
    return defaultResponse
  }

  /** Resets all recorded state and configuration to defaults. */
  fun reset() {
    shouldFail = false
    receivedPrompts.clear()
  }
}
