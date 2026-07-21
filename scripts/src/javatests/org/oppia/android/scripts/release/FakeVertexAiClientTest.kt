package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [FakeVertexAiClient]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FakeVertexAiClientTest {
  private lateinit var fake: FakeVertexAiClient

  @Before
  fun setUp() {
    fake = FakeVertexAiClient()
  }

  // ---------------------------------------------------------------------------
  // generateText -- default response
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_defaultResponse_returnsDefaultText() {
    val result = fake.generateText("any prompt")

    assertThat(result).isEqualTo("Fake generated changelog summary.")
  }

  @Test
  fun testGenerateText_customDefaultResponse_returnsCustomText() {
    fake = FakeVertexAiClient(defaultResponse = "Custom summary.")

    val result = fake.generateText("any prompt")

    assertThat(result).isEqualTo("Custom summary.")
  }

  // ---------------------------------------------------------------------------
  // generateText -- prompt recording
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_singleCall_recordsPrompt() {
    fake.generateText("my prompt")

    assertThat(fake.receivedPrompts).containsExactly("my prompt")
  }

  @Test
  fun testGenerateText_multipleCalls_recordsAllPromptsInOrder() {
    fake.generateText("first prompt")
    fake.generateText("second prompt")

    assertThat(fake.receivedPrompts).containsExactly("first prompt", "second prompt").inOrder()
  }

  @Test
  fun testGenerateText_noCalls_receivedPromptsIsEmpty() {
    assertThat(fake.receivedPrompts).isEmpty()
  }

  // ---------------------------------------------------------------------------
  // generateText -- failure simulation
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_shouldFailTrue_throwsIllegalStateException() {
    fake.shouldFail = true

    val exception = assertThrows<IllegalStateException> { fake.generateText("prompt") }

    assertThat(exception).hasMessageThat().contains("simulated Vertex AI failure")
  }

  @Test
  fun testGenerateText_shouldFailTrue_promptIsStillRecorded() {
    fake.shouldFail = true

    try {
      fake.generateText("my prompt")
    } catch (e: IllegalStateException) {
      // Expected.
    }

    assertThat(fake.receivedPrompts).containsExactly("my prompt")
  }

  @Test
  fun testGenerateText_shouldFailTrue_flagResetsAfterThrow() {
    fake.shouldFail = true

    try {
      fake.generateText("first")
    } catch (e: IllegalStateException) {
      // Expected.
    }

    // Second call should succeed since shouldFail auto-resets to false.
    val result = fake.generateText("second")

    assertThat(fake.shouldFail).isFalse()
    assertThat(result).isEqualTo("Fake generated changelog summary.")
  }

  @Test
  fun testGenerateText_shouldFailFalse_doesNotThrow() {
    fake.shouldFail = false

    // Should not throw.
    fake.generateText("prompt")
  }

  // ---------------------------------------------------------------------------
  // reset()
  // ---------------------------------------------------------------------------

  @Test
  fun testReset_afterPrompts_clearsReceivedPrompts() {
    fake.generateText("prompt one")
    fake.generateText("prompt two")

    fake.reset()

    assertThat(fake.receivedPrompts).isEmpty()
  }

  @Test
  fun testReset_whenShouldFailIsTrue_resetsFlagToFalse() {
    fake.shouldFail = true

    fake.reset()

    assertThat(fake.shouldFail).isFalse()
  }

  @Test
  fun testReset_afterReset_generatesTextNormally() {
    fake.shouldFail = true
    fake.reset()

    val result = fake.generateText("prompt after reset")

    assertThat(result).isEqualTo("Fake generated changelog summary.")
  }
}
