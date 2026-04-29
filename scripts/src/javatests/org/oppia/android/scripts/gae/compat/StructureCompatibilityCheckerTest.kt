package org.oppia.android.scripts.gae.compat

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.Companion.checkMathTagsForLatex
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagHasInvalidContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagMissingContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagMissingRawLatex
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContainerId

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StructureCompatibilityCheckerTest {
  private val testOrigin: ContainerId = ContainerId.Exploration("test_exp_id")
  private val testContentId = "test_content_id"

  @Test
  fun testCheckMathTagsForLatex_htmlWithNoMathTags_returnsNoFailures() {
    val html = "<p>Simple text with no math.</p>"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).isEmpty()
  }

  @Test
  fun testCheckMathTagsForLatex_validMathTagWithRawLatex_returnsNoFailures() {
    val html = buildMathTagHtml(rawLatex = "\\frac{1}{2}")

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).isEmpty()
  }

  @Test
  fun testCheckMathTagsForLatex_mathTagWithEmptyRawLatex_returnsMissingRawLatexFailure() {
    val html = buildMathTagHtml(rawLatex = "")

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingRawLatex::class.java)
    assertThat((failures.first() as MathTagMissingRawLatex).contentId)
      .isEqualTo(testContentId)
  }

  @Test
  fun testCheckMathTagsForLatex_mathTagWithMissingContent_returnsMissingContentFailure() {
    val html =
      "<oppia-noninteractive-math></oppia-noninteractive-math>"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingContent::class.java)
  }

  @Test
  fun testCheckMathTagsForLatex_multipleMathTags_oneMissingContent_returnsSingleFailure() {
    val validTag = buildMathTagHtml(rawLatex = "x^2")
    val invalidTag =
      "<oppia-noninteractive-math></oppia-noninteractive-math>"
    val html = "$validTag $invalidTag"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingContent::class.java)
  }

  @Test
  fun testCheckMathTagsForLatex_mathTagWithMalformedContent_returnsInvalidContentFailure() {
    val malformedContent =
      "{&amp;quot;raw_latex&amp;quot;:&amp;quot;x^2&amp;quot;"
    val html =
      "<oppia-noninteractive-math math_content-with-value=\"$malformedContent\">" +
        "</oppia-noninteractive-math>"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagHasInvalidContent::class.java)
  }

  @Test
  fun testCheckMathTagsForLatex_multipleMathTags_allValid_returnsNoFailures() {
    val tag1 = buildMathTagHtml(rawLatex = "x^2")
    val tag2 = buildMathTagHtml(rawLatex = "\\sqrt{2}")
    val html = "$tag1 $tag2"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).isEmpty()
  }

  private fun buildMathTagHtml(rawLatex: String): String {
    val escapedRawLatex = rawLatex.replace("\\", "\\\\").replace("\"", "\\\"")
    val escapedContent =
      "{&amp;quot;raw_latex&amp;quot;:&amp;quot;$escapedRawLatex&amp;quot;" +
        ",&amp;quot;svg_filename&amp;quot;:&amp;quot;math.svg&amp;quot;}"
    return "<oppia-noninteractive-math math_content-with-value=\"$escapedContent\">" +
      "</oppia-noninteractive-math>"
  }
}
