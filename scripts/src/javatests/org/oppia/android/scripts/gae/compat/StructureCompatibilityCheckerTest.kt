package org.oppia.android.scripts.gae.compat

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.Companion.checkMathTagsForLatex
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.Companion.checkStudyGuideSectionsForCompatibility
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityConstraints
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.HtmlUnexpectedlyInUnicodeContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagHasInvalidContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagMissingContent
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.MathTagMissingRawLatex
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.TextHasInvalidTags
import org.oppia.android.scripts.gae.json.GaeStudyGuideSection
import org.oppia.android.scripts.gae.json.GaeSubtitledHtml
import org.oppia.android.scripts.gae.json.GaeSubtitledUnicode
import org.oppia.android.scripts.gae.proto.LocalizationTracker.ContainerId

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StructureCompatibilityCheckerTest {
  private val testOrigin: ContainerId = ContainerId.Exploration("test_exp_id")
  private val testContentId = "test_content_id"
  private val testConstraints = CompatibilityConstraints(
    supportedInteractionIds = setOf(),
    supportedDefaultLanguages = setOf(),
    requiredTranslationLanguages = setOf(),
    supportedImageFormats = setOf("png"),
    supportedAudioFormats = setOf("mp3"),
    supportedHtmlTags = setOf("p", "strong", "oppia-noninteractive-math"),
    supportedStateSchemaVersion = 55,
    topicDependencies = mapOf(),
    forcedVersions = null
  )

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
  fun testCheckMathTagsForLatex_mathTagWithInvalidJsonSyntax_returnsInvalidContentFailure() {
    val invalidSyntaxContent =
      "{&amp;quot;raw_latex&amp;quot;:," +
        "&amp;quot;svg_filename&amp;quot;:&amp;quot;math.svg&amp;quot;}"
    val html =
      "<oppia-noninteractive-math math_content-with-value=\"$invalidSyntaxContent\">" +
        "</oppia-noninteractive-math>"

    val failures = checkMathTagsForLatex(html, testOrigin, testContentId)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagHasInvalidContent::class.java)
  }

  @Test
  fun testCheckMathTagsForLatex_mathTagWithMissingJsonField_returnsInvalidContentFailure() {
    val missingFieldContent = "{&amp;quot;raw_latex&amp;quot;:&amp;quot;x^2&amp;quot;}"
    val html =
      "<oppia-noninteractive-math math_content-with-value=\"$missingFieldContent\">" +
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

  @Test
  fun testCheckStudyGuideSections_validHeadingAndContent_returnsNoFailures() {
    val sections = listOf(
      createSection(headingText = "What is a fraction?", contentHtml = "<p>Part of a whole.</p>")
    )

    val failures = checkStudyGuideSectionsForCompatibility(sections, testOrigin, testConstraints)

    assertThat(failures).isEmpty()
  }

  @Test
  fun testCheckStudyGuideSections_headingContainsHtml_returnsHtmlInUnicodeContentFailure() {
    val sections = listOf(
      createSection(headingText = "<p>What is a fraction?</p>", contentHtml = "<p>Part.</p>")
    )

    val failures = checkStudyGuideSectionsForCompatibility(sections, testOrigin, testConstraints)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(HtmlUnexpectedlyInUnicodeContent::class.java)
    assertThat((failures.first() as HtmlUnexpectedlyInUnicodeContent).contentId)
      .isEqualTo("heading_0")
  }

  @Test
  fun testCheckStudyGuideSections_contentHasUnsupportedHtmlTag_returnsInvalidTagsFailure() {
    val sections = listOf(
      createSection(headingText = "Heading", contentHtml = "<blink>Part of a whole.</blink>")
    )

    val failures = checkStudyGuideSectionsForCompatibility(sections, testOrigin, testConstraints)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(TextHasInvalidTags::class.java)
    assertThat((failures.first() as TextHasInvalidTags).invalidTagNames).containsExactly("blink")
  }

  @Test
  fun testCheckStudyGuideSections_contentMathTagWithEmptyRawLatex_returnsMathFailure() {
    val sections = listOf(
      createSection(headingText = "Heading", contentHtml = buildMathTagHtml(rawLatex = ""))
    )

    val failures = checkStudyGuideSectionsForCompatibility(sections, testOrigin, testConstraints)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(MathTagMissingRawLatex::class.java)
  }

  @Test
  fun testCheckStudyGuideSections_multipleSections_oneInvalidHeading_returnsSingleFailure() {
    val sections = listOf(
      createSection(headingText = "Valid heading", contentHtml = "<p>Valid content.</p>"),
      createSection(headingText = "<strong>Invalid</strong>", contentHtml = "<p>Also valid.</p>")
    )

    val failures = checkStudyGuideSectionsForCompatibility(sections, testOrigin, testConstraints)

    assertThat(failures).hasSize(1)
    assertThat(failures.first()).isInstanceOf(HtmlUnexpectedlyInUnicodeContent::class.java)
  }

  private fun createSection(headingText: String, contentHtml: String): GaeStudyGuideSection {
    return GaeStudyGuideSection(
      heading = GaeSubtitledUnicode(contentId = "heading_0", text = headingText),
      content = GaeSubtitledHtml(contentId = "content_1", text = contentHtml)
    )
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
