package org.oppia.android.scripts.gae.compat

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityConstraints
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagMissingRawLatex
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContainerId

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StructureCompatibilityCheckerTest {
  private lateinit var checker: StructureCompatibilityChecker
  private val testOrigin: ContainerId = ContainerId.Exploration("test_exp_id")
  private val testContentId = "test_content_id"

  @Before
  fun setUp() {
    checker = StructureCompatibilityChecker(
      constraints = CompatibilityConstraints(
        supportedInteractionIds = emptySet(),
        supportedDefaultLanguages = emptySet(),
        requiredTranslationLanguages = emptySet(),
        supportedImageFormats = emptySet(),
        supportedAudioFormats = emptySet(),
        supportedHtmlTags = setOf("oppia-noninteractive-math"),
        supportedStateSchemaVersion = 0,
        topicDependencies = emptyMap(),
        forcedVersions = null
      ),
      localizationTracker = mock(),
      subtitledHtmlCollector = mock()
    )
  }

  @Test
  fun testCheckHasValidMathTags_htmlWithNoMathTags_returnsNoFailures() {
    val html = "<p>Simple text with no math.</p>"

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).isEmpty()
  }

  @Test
  fun testCheckHasValidMathTags_validMathTagWithRawLatex_returnsNoFailures() {
    val html = buildMathTagHtml(rawLatex = "\\frac{1}{2}")

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).isEmpty()
  }

  @Test
  fun testCheckHasValidMathTags_mathTagWithEmptyRawLatex_returnsMissingRawLatexFailure() {
    val html = buildMathTagHtml(rawLatex = "")

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingRawLatex::class.java)
    assertThat((failures.first() as MathTagMissingRawLatex).contentId)
      .isEqualTo(testContentId)
  }

  @Test
  fun testCheckHasValidMathTags_mathTagWithMissingContent_returnsMissingRawLatexFailure() {
    val html =
      "<oppia-noninteractive-math></oppia-noninteractive-math>"

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingRawLatex::class.java)
  }

  @Test
  fun testCheckHasValidMathTags_multipleMathTags_oneInvalid_returnsOneFailure() {
    val validTag = buildMathTagHtml(rawLatex = "x^2")
    val invalidTag =
      "<oppia-noninteractive-math></oppia-noninteractive-math>"
    val html = "$validTag $invalidTag"

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingRawLatex::class.java)
  }

  @Test
  fun testCheckHasValidMathTags_multipleMathTags_allValid_returnsNoFailures() {
    val tag1 = buildMathTagHtml(rawLatex = "x^2")
    val tag2 = buildMathTagHtml(rawLatex = "\\sqrt{2}")
    val html = "$tag1 $tag2"

    val failures = with(checker) { html.checkHasValidMathTags(testOrigin, testContentId) }

    assertThat(failures).isEmpty()
  }

  private fun buildMathTagHtml(rawLatex: String): String {
    val escapedContent = "{&amp;quot;raw_latex&amp;quot;:&amp;quot;$rawLatex&amp;quot;}"
    return "<oppia-noninteractive-math math_content-with-value=\"$escapedContent\">" +
      "</oppia-noninteractive-math>"
  }
}
