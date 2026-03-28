package org.oppia.android.scripts.gae.proto

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.extractMathContentsFromHtml
import kotlin.test.assertFailsWith

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

    assertFailsWith<Exception> {
      extractMathContentsFromHtml(html)
    }
  }
}
