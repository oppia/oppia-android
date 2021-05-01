package org.oppia.android.util.parser

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.annotation.LooperMode
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl
import kotlin.reflect.KClass

/** Tests for [CustomHtmlContentHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CustomHtmlContentHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockImageRetriever: FakeImageRetriever

  @Test
  fun testParseHtml_emptyString_returnsEmptyString() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "", imageRetriever = mockImageRetriever, customTagHandlers = mapOf()
      )

    assertThat(parsedHtml.length).isEqualTo(0)
  }

  @Test
  fun testParseHtml_standardBoldHtml_returnsStringWithBoldSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "<strong>Text</strong>",
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf()
      )

    assertThat(parsedHtml.toString()).isEqualTo("Text")
    assertThat(parsedHtml.getSpansFromWholeString(StyleSpan::class)).hasLength(1)
  }

  @Test
  fun testParseHtml_withImage_callsImageGetter() {
    CustomHtmlContentHandler.fromHtml(
      html = "<img src=\"test_source.png\"></img>",
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf()
    )

    verify(mockImageRetriever).getDrawable(anyString())
  }

  @Test
  fun testParseHtml_withOneCustomTag_handlerIsCalledWithAttributes() {
    val fakeTagHandler = FakeTagHandler()

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "<custom-tag custom-attribute=\"value\">content</custom-tag>",
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf("custom-tag" to fakeTagHandler)
      )

    assertThat(fakeTagHandler.handleTagCalled).isTrue()
    assertThat(fakeTagHandler.attributes.getValue("custom-attribute")).isEqualTo("value")
    assertThat(parsedHtml.toString()).isEqualTo("content")
  }

  @Test
  fun testParseHtml_withOneCustomTag_nonAttributeHandlersAreCalledBeforeAttributeAndInOrder() {
    val fakeTagHandler = FakeTagHandler()

    CustomHtmlContentHandler.fromHtml(
      html = "<custom-tag custom-attribute=\"value\">content</custom-tag>",
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf("custom-tag" to fakeTagHandler)
    )

    assertThat(fakeTagHandler.handleOpeningTagCalled).isTrue()
    assertThat(fakeTagHandler.handleClosingTagCalled).isTrue()
    assertThat(fakeTagHandler.handleTagCalled).isTrue()
    // Call order: opening tag -> close tag -> handle tag.
    assertThat(fakeTagHandler.handleOpeningTagCallIndex)
      .isLessThan(fakeTagHandler.handleClosingTagCallIndex)
    assertThat(fakeTagHandler.handleClosingTagCallIndex)
      .isLessThan(fakeTagHandler.handleTagCallIndex)
  }

  @Test
  fun testParseHtml_withOneCustomTag_missingHandler_keepsContent() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "<custom-tag custom-attribute=\"value\">content</custom-tag>",
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf()
      )

    assertThat(parsedHtml.toString()).isEqualTo("content")
  }

  @Test
  fun testParseHtml_withOneCustomTag_handlerReplacesText_correctlyUpdatesText() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "<custom-tag custom-attribute=\"value\">content</custom-tag>",
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          "custom-tag" to ReplacingTagHandler("custom-attribute")
        )
      )

    // Verify that handlers which wish to replace text can successfully do so.
    assertThat(parsedHtml.toString()).isEqualTo("value")
  }

  @Test
  fun testParseHtml_withNestedTags_successfullyParsesBoth() {
    val outerFakeTagHandler = FakeTagHandler()
    val innerFakeTagHandler = FakeTagHandler()

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "<outer-tag>some <inner-tag>other</inner-tag> content</outer-tag>",
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          "outer-tag" to outerFakeTagHandler,
          "inner-tag" to innerFakeTagHandler
        )
      )

    // Verify that both tag handlers are called (showing support for nesting).
    assertThat(outerFakeTagHandler.handleTagCalled).isTrue()
    assertThat(innerFakeTagHandler.handleTagCalled).isTrue()
    assertThat(parsedHtml.toString()).isEqualTo("some other content")
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoBulletSpan() {
    val htmlString = "<p>Paragraph 1</p><ul><oppia-li>Item</oppia-li></ul><p>Paragraph 2.</p>"

    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = htmlString,
        imageRetriever = mockImageRetriever,
        customTagHandlers = mapOf(
          CUSTOM_BULLET_LIST_TAG to BulletTagHandler()
        )
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(BulletSpan::class)).hasLength(1)
  }

  @Test
  fun testAttributeHelpers_getJsonStringValue_valueMissing_returnsNull() {
    val attributes = AttributesImpl()

    val value = attributes.getJsonStringValue("missing_attrib")

    assertThat(value).isNull()
  }

  @Test
  fun testAttributeHelpers_getJsonStringValue_valuePresent_returnsValue() {
    val attributes = AttributesImpl()
    attributes.addAttribute(name = "attrib", value = "value")

    val value = attributes.getJsonStringValue("attrib")

    assertThat(value).isEqualTo("value")
  }

  @Test
  fun testAttributeHelpers_getJsonStringValue_valueWithEscapedQuotes_returnsValueWithoutQuotes() {
    val attributes = AttributesImpl()
    attributes.addAttribute(name = "attrib", value = "&quot;value&quot;")

    val value = attributes.getJsonStringValue("attrib")

    assertThat(value).isEqualTo("value")
  }

  @Test
  fun testAttributeHelpers_getJsonObjectValue_valueMissing_returnsNull() {
    val attributes = AttributesImpl()

    val value = attributes.getJsonObjectValue("missing_attrib")

    assertThat(value).isNull()
  }

  @Test
  fun testAttributeHelpers_getJsonObjectValue_invalidJson_returnsNull() {
    val attributes = AttributesImpl()
    attributes.addAttribute(name = "attrib", value = "{")

    val value = attributes.getJsonObjectValue("attrib")

    assertThat(value).isNull()
  }

  @Test
  fun testAttributeHelpers_getJsonObjectValue_quotedAndEscapedJson_returnsValidJsonObject() {
    val attributes = AttributesImpl()
    attributes.addAttribute(
      name = "attrib",
      value = "{&quot;key&quot;:&quot;value with \\\\frac{1}{2}&quot;}"
    )

    val jsonObject = attributes.getJsonObjectValue("attrib")

    assertThat(jsonObject).isNotNull()
    assertThat(jsonObject?.has("key")).isTrue()
    assertThat(jsonObject?.getString("key")).isEqualTo("value with \\frac{1}{2}")
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun AttributesImpl.addAttribute(name: String, value: String) {
    addAttribute(
      /* uri= */ null,
      /* localName= */ null,
      /* qName= */ name,
      /* type= */ "string",
      value
    )
  }

  private class FakeTagHandler : CustomHtmlContentHandler.CustomTagHandler {
    var handleTagCalled = false
    var handleTagCallIndex = -1
    var handleOpeningTagCalled = false
    var handleOpeningTagCallIndex = -1
    var handleClosingTagCalled = false
    var handleClosingTagCallIndex = -1
    lateinit var attributes: Attributes
    private var methodCallCount = 0

    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: CustomHtmlContentHandler.ImageRetriever
    ) {
      handleTagCalled = true
      handleTagCallIndex = methodCallCount++
      this.attributes = attributes
    }

    override fun handleOpeningTag(output: Editable) {
      handleOpeningTagCalled = true
      handleOpeningTagCallIndex = methodCallCount++
    }

    override fun handleClosingTag(output: Editable) {
      handleClosingTagCalled = true
      handleClosingTagCallIndex = methodCallCount++
    }
  }

  private class ReplacingTagHandler(
    private val attributeTextToReplaceWith: String
  ) : CustomHtmlContentHandler.CustomTagHandler {
    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: CustomHtmlContentHandler.ImageRetriever
    ) {
      output.replace(openIndex, closeIndex, attributes.getValue(attributeTextToReplaceWith))
    }
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever
}
