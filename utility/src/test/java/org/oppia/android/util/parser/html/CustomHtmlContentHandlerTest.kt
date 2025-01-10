package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.StyleSpan
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.AndroidLocaleFactory
import org.oppia.android.util.locale.DisplayLocaleImpl
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.robolectric.annotation.LooperMode
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val UL_TAG_MARKUP_1 =
  "<p>Paragraph 1</p><oppia-ul><oppia-li>Item</oppia-li></oppia-ul>" +
    "<p>Paragraph 2.</p>"

private const val OL_TAG_MARKUP_1 =
  "<p>Paragraph 1</p><oppia-ol><oppia-li>Item</oppia-li></oppia-ol>" +
    "<p>Paragraph 2.</p>"

/** Tests for [CustomHtmlContentHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class CustomHtmlContentHandlerTest {
  private lateinit var tagHandlersWithListTagSupport: Map<String, CustomTagHandler>

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var androidLocaleFactory: AndroidLocaleFactory

  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Mock
  private var mockImageRetriever: FakeImageRetriever? = null

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    val displayLocale = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val liTaghandler = LiTagHandler(context, displayLocale)
    tagHandlersWithListTagSupport = mapOf(
      CUSTOM_LIST_OL_TAG to liTaghandler,
      CUSTOM_LIST_UL_TAG to liTaghandler,
      CUSTOM_LIST_LI_TAG to liTaghandler
    )
  }

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

    verify(mockImageRetriever)!!.getDrawable(anyString())
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
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = UL_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithListTagSupport
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan.UlSpan::class))
      .hasLength(1)
  }

  @Test
  fun testCustomListElement_betweenParagraphs_parsesCorrectlyIntoNumberedListSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = OL_TAG_MARKUP_1,
        imageRetriever = mockImageRetriever,
        customTagHandlers = tagHandlersWithListTagSupport
      )

    assertThat(parsedHtml.toString()).isNotEmpty()
    assertThat(parsedHtml.getSpansFromWholeString(ListItemLeadingMarginSpan.OlSpan::class))
      .hasLength(1)
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

  @Test
  fun testGetContentDescription_withNestedTags_handlesNestingCorrectly() {
    val outerHandler = FakeContentDescriptionTagHandler("Outer Tag ")
    val innerHandler = FakeContentDescriptionTagHandler("Inner Tag ")

    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      html = "<outer-tag>before <inner-tag>nested</inner-tag> after</outer-tag>",
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf(
        "outer-tag" to outerHandler,
        "inner-tag" to innerHandler
      )
    )

    assertThat(contentDescription).isEqualTo("Outer Tag before Inner Tag nested after")
  }

  @Test
  fun testGetContentDescription_withMultipleTags_preservesOrder() {
    val firstHandler = FakeContentDescriptionTagHandler("First ")
    val secondHandler = FakeContentDescriptionTagHandler("Second ")
    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      html = "Start <first-tag>one</first-tag> middle <second-tag>two</second-tag> end",
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf(
        "first-tag" to firstHandler,
        "second-tag" to secondHandler
      )
    )

    assertThat(contentDescription).isEqualTo("Start First one middle Second two end")
  }

  @Test
  fun testGetContentDescription_whitespaceHandling_normalizedCorrectly() {
    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      html =
        """
        <p>First    paragraph</p>
        
        <p>Second     paragraph</p>
        
        <p>Third    paragraph</p>
        """.trimIndent(),
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf()
    )

    assertThat(contentDescription).isEqualTo(
      "First    paragraph\n" +
        "Second     paragraph\n" +
        "Third    paragraph"
    )
  }

  @Test
  fun testGetContentDescription_blockElements_preserveStructure() {
    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      html =
        """
        <header>Header text</header>
        <article>Article content</article>
        <section>Section text</section>
        <aside>Aside content</aside>
        <footer>Footer text</footer>
        """.trimIndent(),
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf()
    )

    assertThat(contentDescription).isEqualTo(
      "Header text\n" +
        "Article content\n" +
        "Section text\n" +
        "Aside content\n" +
        "Footer text"
    )
  }

  @Test
  fun testGetContentDescription_mixedContentTypes_handlesCorrectly() {
    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      html =
        """
        <p>Regular paragraph</p>
        <custom-tag>Custom content</custom-tag>
        <ul><li>List item 1</li><li>List item 2</li></ul>
        <custom-tag>More custom</custom-tag>
        <p>Final paragraph</p>
        """.trimIndent(),
      imageRetriever = mockImageRetriever,
      customTagHandlers = mapOf()
    )

    assertThat(contentDescription).isEqualTo(
      "Regular paragraph\n" +
        "Custom content\n" +
        "List item 1\n" +
        "List item 2\n" +
        "More custom\n" +
        "Final paragraph"
    )
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

  private fun createDisplayLocaleImpl(context: OppiaLocaleContext): DisplayLocaleImpl {
    val formattingLocale = androidLocaleFactory.createOneOffAndroidLocale(context)
    return DisplayLocaleImpl(context, formattingLocale, machineLocale, formatterFactory)
  }

  private class FakeContentDescriptionTagHandler(
    private val contentDesc: String
  ) : CustomTagHandler, CustomHtmlContentHandler.ContentDescriptionProvider {
    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: CustomHtmlContentHandler.ImageRetriever?
    ) {}

    override fun getContentDescription(attributes: Attributes): String {
      return contentDesc
    }
  }
  private class FakeTagHandler : CustomTagHandler {
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
      imageRetriever: CustomHtmlContentHandler.ImageRetriever?
    ) {
      handleTagCalled = true
      handleTagCallIndex = methodCallCount++
      this.attributes = attributes
    }

    override fun handleOpeningTag(
      output: Editable,
      tag: String
    ) {
      handleOpeningTagCalled = true
      handleOpeningTagCallIndex = methodCallCount++
    }

    override fun handleClosingTag(
      output: Editable,
      indentation: Int,
      tag: String
    ) {
      handleClosingTagCalled = true
      handleClosingTagCallIndex = methodCallCount++
    }
  }

  private class ReplacingTagHandler(
    private val attributeTextToReplaceWith: String
  ) : CustomTagHandler {
    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable,
      imageRetriever: CustomHtmlContentHandler.ImageRetriever?
    ) {
      output.replace(openIndex, closeIndex, attributes.getValue(attributeTextToReplaceWith))
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerCustomHtmlContentHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, RobolectricModule::class,
      FakeOppiaClockModule::class, LoggerModule::class, LocaleProdModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(customHtmlContentHandlerTest: CustomHtmlContentHandlerTest)
  }

  private companion object {

    private val US_ENGLISH_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ENGLISH
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "en"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
      }.build()
    }.build()
  }
}
