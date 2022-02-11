package org.oppia.android.app.policies

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.text.Spannable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PoliciesFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PoliciesFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class PoliciesFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  @get:Rule
  var activityTestRule: ActivityTestRule<PoliciesActivity> = ActivityTestRule(
    PoliciesActivity::class.java, /* initialTouchMode= */
    true, /* launchActivity= */
    false
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPrivacyPolicyFragment_checkPrivacyPolicy_isDisplayed() {
    launch<PoliciesActivity>(createPoliciesActivity(PolicyPage.PRIVACY_POLICY)).use {
      onView(withId(R.id.policies_description_text_view)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPrivacyPolicyFragment_checkPrivacyPolicyWebLink_isDisplayed() {
    launch<PoliciesActivity>(createPoliciesActivity(PolicyPage.PRIVACY_POLICY)).use {
      onView(withId(R.id.policies_web_link_text_view)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPrivacyPolicyFragment_checkTermsOfService_isDisplayed() {
    launch<PoliciesActivity>(createPoliciesActivity(PolicyPage.TERMS_OF_SERVICE)).use {
      onView(withId(R.id.policies_description_text_view)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPrivacyPolicyFragment_checkTermsOfServiceWebLink_isDisplayed() {
    launch<PoliciesActivity>(createPoliciesActivity(PolicyPage.TERMS_OF_SERVICE)).use {
      onView(withId(R.id.policies_web_link_text_view)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPrivacyPolicyFragment_checkPrivacyPolicy_isCorrectlyParsed() {
    activityTestRule.launchActivity(createPoliciesActivity(PolicyPage.PRIVACY_POLICY))
    val privacyPolicyTextView = activityTestRule.activity.findViewById(
      R.id.policies_description_text_view
    ) as TextView
    val htmlParser = htmlParserFactory.create()
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      getResources().getString(R.string.privacy_policy_content),
      privacyPolicyTextView
    )
    assertThat(privacyPolicyTextView.text.toString()).isEqualTo(htmlResult.toString())
  }

  @Test
  fun testPrivacyPolicyFragment_checkTermsOfService_isCorrectlyParsed() {
    activityTestRule.launchActivity(createPoliciesActivity(PolicyPage.TERMS_OF_SERVICE))
    val privacyPolicyTextView = activityTestRule.activity.findViewById(
      R.id.policies_description_text_view
    ) as TextView
    val htmlParser = htmlParserFactory.create()
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      getResources().getString(R.string.terms_of_service_content),
      privacyPolicyTextView
    )
    assertThat(privacyPolicyTextView.text.toString()).isEqualTo(htmlResult.toString())
  }

  private fun createPoliciesActivity(policies: PolicyPage): Intent {
    return when (policies) {
      PolicyPage.PRIVACY_POLICY -> {
        PoliciesActivity.createPoliciesActivityIntent(
          ApplicationProvider.getApplicationContext(),
          PolicyPage.PRIVACY_POLICY
        )
      }
      PolicyPage.TERMS_OF_SERVICE -> {
        PoliciesActivity.createPoliciesActivityIntent(
          ApplicationProvider.getApplicationContext(),
          PolicyPage.TERMS_OF_SERVICE
        )
      }
      else -> PoliciesActivity.createPoliciesActivityIntent(
        ApplicationProvider.getApplicationContext(),
        PolicyPage.POLICY_PAGE_UNSPECIFIED
      )
    }
  }

  private fun getResources(): Resources {
    return ApplicationProvider.getApplicationContext<Context>().resources
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(privacyPolicyFragmentTest: PoliciesFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPoliciesFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(privacyPolicyFragmentTest: PoliciesFragmentTest) {
      component.inject(privacyPolicyFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
