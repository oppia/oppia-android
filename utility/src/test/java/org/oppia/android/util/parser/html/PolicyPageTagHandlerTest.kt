package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.style.ClickableSpan
import android.widget.TextView
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
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.mockito.capture
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.CustomTagHandler
import org.oppia.android.util.parser.html.PolicyPageTagHandler.PolicyPageLinkClickListener
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

private const val POLICY_PAGE_LINK_MARKUP_1 =
  "By using %s, you agree to our <br> <oppia-noninteractive-policy link=\"tos\">Terms of Service" +
    "</oppia-noninteractive-policy> and <oppia-noninteractive-policy link=\"privacy\">" +
    "Privacy Policy</oppia-noninteractive-policy>."

/** Tests for [PolicyPageTagHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PolicyPageTagHandlerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockPolicyPageLinkClickListener: PolicyPageLinkClickListener

  @Captor
  lateinit var policyTypeCaptor: ArgumentCaptor<PolicyType>

  @Inject lateinit var context: Context
  @Inject lateinit var consoleLogger: ConsoleLogger

  private lateinit var noTagHandlers: Map<String, CustomTagHandler>
  private lateinit var tagHandlersWithPolicyPageSupport: Map<String, CustomTagHandler>
  private lateinit var testView: TextView

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    noTagHandlers = mapOf()
    tagHandlersWithPolicyPageSupport = mapOf(
      CUSTOM_POLICY_PAGE_TAG to PolicyPageTagHandler(
        mockPolicyPageLinkClickListener,
        consoleLogger
      )
    )
    testView = TextView(context)
  }

  // TODO(#3085): Introduce test for verifying that the error log scenario is logged correctly.

  @Test
  fun testParseHtml_emptyString_doesNotIncludeClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = "",
        imageRetriever = null,
        customTagHandlers = tagHandlersWithPolicyPageSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
  }

  @Test
  fun testParseHtml_withPolicyPageMarkup_includesClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = POLICY_PAGE_LINK_MARKUP_1,
        imageRetriever = null,
        customTagHandlers = tagHandlersWithPolicyPageSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).hasLength(2)
  }

  @Test
  fun testParseHtml_withPolicyPageMarkup_clickSpan_callsClickListenerForPrivacyPolicy() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = POLICY_PAGE_LINK_MARKUP_1,
        imageRetriever = null,
        customTagHandlers = tagHandlersWithPolicyPageSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    clickableSpans[1].onClick(testView)
    verify(mockPolicyPageLinkClickListener)
      .onPolicyPageLinkClicked(capture(policyTypeCaptor))

    assertThat(policyTypeCaptor.value).isEqualTo(PolicyType.PRIVACY_POLICY)
  }

  @Test
  fun testGetContentDescription_withPolicyPageTag() {
    val contentDescription =
      CustomHtmlContentHandler.getContentDescription(
        html = POLICY_PAGE_LINK_MARKUP_1,
        imageRetriever = null,
        customTagHandlers = tagHandlersWithPolicyPageSupport
      )

    assertThat(contentDescription).isEqualTo(
      "By using %s, you agree to our " +
        " Link to Terms of Service and Link to Privacy Policy."
    )
  }

  @Test
  fun testParseHtml_withPolicyPageMarkup_clickSpan_callsClickListenerForTermsOfService() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = POLICY_PAGE_LINK_MARKUP_1,
        imageRetriever = null,
        customTagHandlers = tagHandlersWithPolicyPageSupport
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    clickableSpans[0].onClick(testView)
    verify(mockPolicyPageLinkClickListener)
      .onPolicyPageLinkClicked(capture(policyTypeCaptor))

    assertThat(policyTypeCaptor.value).isEqualTo(PolicyType.TERMS_OF_SERVICE)
  }

  @Test
  fun testParseHtml_noTagHandler_withPolicyPageMarkup_doesNotIncludeClickableSpan() {
    val parsedHtml =
      CustomHtmlContentHandler.fromHtml(
        html = POLICY_PAGE_LINK_MARKUP_1,
        imageRetriever = null,
        customTagHandlers = noTagHandlers
      )

    val clickableSpans = parsedHtml.getSpansFromWholeString(ClickableSpan::class)
    assertThat(clickableSpans).isEmpty()
  }

  private fun <T : Any> Spannable.getSpansFromWholeString(spanClass: KClass<T>): Array<T> =
    getSpans(/* start= */ 0, /* end= */ length, spanClass.javaObjectType)

  private fun setUpTestApplicationComponent() {
    DaggerPolicyPageTagHandlerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

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

    fun inject(PolicyPageTagHandlerTest: PolicyPageTagHandlerTest)
  }

  /**
   * A fake image retriever that satisfies both the contracts of [Html.ImageGetter] and
   * [CustomHtmlContentHandler.ImageRetriever].
   */
  interface FakeImageRetriever : Html.ImageGetter, CustomHtmlContentHandler.ImageRetriever
}
