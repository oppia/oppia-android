package org.oppia.android.app.parser

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.getSpans
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.MetricLogSchedulerModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.CustomBulletSpan
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CustomBulletSpanTest.TestApplication::class, qualifiers = "port-xxhdpi")
class CustomBulletSpanTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private var context: Context = ApplicationProvider.getApplicationContext<TestApplication>()

  private val testStringWithoutBulletSpan = SpannableString("Text Without BulletSpan")
  private val testStringWithBulletSpan = SpannableString("Text With \nBullet Point").apply {
    setSpan(BulletSpan(), 10, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  private val testStringWithMultipleBulletSpan = SpannableString(
    "Text With \nfirst \nsecond \nthird \nfour \nfive"
  ).apply {
    setSpan(BulletSpan(), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 18, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 27, 35, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(UnderlineSpan(), 42, 43, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  private val testStringWithCustomBulletSpan = SpannableString("Text With \nBullet Point").apply {
    this.setSpan(
      CustomBulletSpan(context),
      10,
      22,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_spannableStringWithoutBulletSpanRemainSame() {
    val spannableString1 = testStringWithoutBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(0)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_spannableStringWithBulletSpan_isNotSame() {
    val spannableString1 = testStringWithBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)

    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_includingUnderlineSpan_underlineSpanRemainsSame() {
    val spannableString1 = testStringWithMultipleBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(4)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getUnderlineSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(4)
    assertThat(getUnderlineSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_customBulletSpans_remainsSame() {
    val spannableString1 = testStringWithCustomBulletSpan
    val convertedSpannableStringBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(spannableString1),
      context
    )
    val spannableString2 = SpannableString.valueOf(convertedSpannableStringBuilder)
    assertThat(getBulletSpanCount(spannableString1)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString1)).isEqualTo(1)
    assertThat(getBulletSpanCount(spannableString2)).isEqualTo(0)
    assertThat(getCustomBulletSpanCount(spannableString2)).isEqualTo(1)
  }

  @Test
  fun customBulletSpan_testLeadMargin_isComputedToProperlyIndentText() {
    val bulletRadius = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.bullet_radius
    )
    val spacingBeforeBullet = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_bullet
    )
    val spacingBeforeText = context.resources.getDimensionPixelSize(
      org.oppia.android.util.R.dimen.spacing_before_text
    )
    val expectedMargin = spacingBeforeBullet + spacingBeforeText + 2 * bulletRadius
    val spannableString = SpannableStringBuilder(testStringWithBulletSpan)
    val customBulletSpannable = CustomBulletSpan.replaceBulletSpan(spannableString, context)
    val leadingMargin = customBulletSpannable.getSpans(
      0,
      spannableString.length,
      CustomBulletSpan::class.java
    )[0].getLeadingMargin(true)
    assertThat(leadingMargin).isEqualTo(expectedMargin)
  }

  private fun getBulletSpans(spannableString: SpannableString): Array<out BulletSpan> {
    return spannableString.getSpans<BulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getCustomBulletSpans(
    spannableString: SpannableString
  ): Array<out CustomBulletSpan> {
    return spannableString.getSpans<CustomBulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getUnderlineSpans(spannableString: SpannableString): Array<out UnderlineSpan> {
    return spannableString.getSpans<UnderlineSpan>(
      0,
      spannableString.length
    )
  }

  private fun getBulletSpanCount(spannableString: SpannableString): Int {
    return getBulletSpans(spannableString).size
  }

  private fun getCustomBulletSpanCount(spannableString: SpannableString): Int {
    return getCustomBulletSpans(spannableString).size
  }

  private fun getUnderlineSpanCount(spannableString: SpannableString): Int {
    return getUnderlineSpans(spannableString).size
  }

  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, PlatformParameterModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(customBulletSpanTest: CustomBulletSpanTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCustomBulletSpanTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(customBulletSpanTest: CustomBulletSpanTest) {
      component.inject(customBulletSpanTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
