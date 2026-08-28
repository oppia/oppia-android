package org.oppia.android.scripts.gae.proto

import com.google.common.truth.Truth.assertThat
import java.io.EOFException
import org.junit.Test
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.expandNestedWorkedExampleHtml
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.extractMathContentsFromHtml
import org.oppia.android.testing.assertThrows

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class LocalizationTrackerTest {
  @Test
  fun testExtractMathContentsFromHtml_noMathTag_returnsEmptyList() {
    val html = "<p>plain content</p>"

    val values = extractMathContentsFromHtml(html)

    assertThat(values).isEmpty()
  }

  @Test
  fun testExtractMathContentsFromHtml_validMathTag_returnsParsedContent() {
    val html =
      "<oppia-noninteractive-math math_content-with-value=\"" +
        "{&amp;quot;raw_latex&amp;quot;:&amp;quot;\\\\frac{1}{2}&amp;quot;," +
        "&amp;quot;svg_filename&amp;quot;:&amp;quot;math.svg&amp;quot;}\">" +
        "</oppia-noninteractive-math>"

    val values = extractMathContentsFromHtml(html)

    assertThat(values).hasSize(1)
    assertThat(values.single().rawLatex).isEqualTo("\\frac{1}{2}")
    assertThat(values.single().svgFilename).isEqualTo("math.svg")
  }

  @Test
  fun testExtractMathContentsFromHtml_malformedContent_throws() {
    val html =
      "<oppia-noninteractive-math math_content-with-value=\"" +
        "{&amp;quot;raw_latex&amp;quot;:&amp;quot;x^2&amp;quot;\"></oppia-noninteractive-math>"

    val exception = assertThrows<EOFException> {
      extractMathContentsFromHtml(html)
    }

    assertThat(exception).hasMessageThat().contains("End of input")
  }

  @Test
  fun testExpandNestedWorkedExampleHtml_noWorkedExample_returnsHtmlUnchanged() {
    val html = "<p>plain content</p>"

    val expanded = expandNestedWorkedExampleHtml(html)

    assertThat(expanded).isEqualTo(html)
  }

  @Test
  fun testExpandNestedWorkedExampleHtml_workedExample_decodesNestedHtml() {
    val expanded = expandNestedWorkedExampleHtml(WORKED_EXAMPLE_WITH_NESTED_IMAGE)

    // The original HTML is preserved, with the decoded question/answer HTML appended.
    assertThat(expanded).startsWith(WORKED_EXAMPLE_WITH_NESTED_IMAGE)
    assertThat(expanded).contains("<p>Plot 10.67:</p>")
    assertThat(expanded).contains("<p>It is 10.67.</p>")
  }

  @Test
  fun testExpandNestedWorkedExampleHtml_nestedImage_isDiscoverableByImageTagRegex() {
    val expanded = expandNestedWorkedExampleHtml(WORKED_EXAMPLE_WITH_NESTED_IMAGE)

    // The nested image tag is left in exactly the form the pipeline's own tag regexes expect.
    assertThat(expanded).contains(
      "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;img_test.svg&amp;quot;\">"
    )
  }

  @Test
  fun testExpandNestedWorkedExampleHtml_nestedMath_isExtractable() {
    val html =
      "<oppia-noninteractive-workedexample answer-with-value=\"" +
        "&amp;quot;&amp;lt;oppia-noninteractive-math math_content-with-value=" +
        "\\&amp;quot;{&amp;amp;amp;quot;raw_latex&amp;amp;amp;quot;:&amp;amp;amp;quot;x^2" +
        "&amp;amp;amp;quot;,&amp;amp;amp;quot;svg_filename&amp;amp;amp;quot;:" +
        "&amp;amp;amp;quot;nested.svg&amp;amp;amp;quot;}\\&amp;quot;&amp;gt;" +
        "&amp;lt;/oppia-noninteractive-math&amp;gt;&amp;quot;\">" +
        "</oppia-noninteractive-workedexample>"

    val values = extractMathContentsFromHtml(expandNestedWorkedExampleHtml(html))

    assertThat(values).hasSize(1)
    assertThat(values.single().svgFilename).isEqualTo("nested.svg")
  }

  private companion object {
    // Mirrors the exact encoding Oppia web produces: the question/answer HTML is doubly
    // HTML-escaped and JSON-encoded into the worked example tag's attributes.
    private const val WORKED_EXAMPLE_WITH_NESTED_IMAGE =
      "<p>Intro</p><oppia-noninteractive-workedexample question-with-value=\"" +
        "&amp;quot;&amp;lt;p&amp;gt;Plot 10.67:&amp;lt;/p&amp;gt;" +
        "&amp;lt;oppia-noninteractive-image filepath-with-value=" +
        "\\&amp;quot;&amp;amp;amp;quot;img_test.svg&amp;amp;amp;quot;\\&amp;quot;&amp;gt;" +
        "&amp;lt;/oppia-noninteractive-image&amp;gt;&amp;quot;\" answer-with-value=\"" +
        "&amp;quot;&amp;lt;p&amp;gt;It is 10.67.&amp;lt;/p&amp;gt;&amp;quot;\">" +
        "</oppia-noninteractive-workedexample>"
  }
}
