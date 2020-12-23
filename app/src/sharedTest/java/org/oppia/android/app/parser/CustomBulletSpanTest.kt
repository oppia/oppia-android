package org.oppia.android.app.parser

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.getSpans
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.CustomBulletSpan
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CustomBulletSpanTest.TestApplication::class, qualifiers = "port-xxhdpi")
class CustomBulletSpanTest {

  private var context: Context = ApplicationProvider.getApplicationContext<TestApplication>()

  val testStringWithoutBulletSpan = SpannableStringBuilder("Text Without BulletSpan")
  val testStringWithBulletSpan = SpannableStringBuilder("Text With \nBullet Point").apply {
    setSpan(BulletSpan(), 10, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  val testStringWithMultipleBulletSpan = SpannableStringBuilder(
    "Text With \nfirst \nsecond \nthird \nfour \nfive"
  ).apply {
    setSpan(BulletSpan(),  10,  18,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(),  18,  27,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(),  27,  35,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(BulletSpan(),  35,  42,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    setSpan(UnderlineSpan(),  42,  43,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  val testStringWithCustomBulletSpan = SpannableStringBuilder("Text With \nBullet Point").apply {
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
    val spannableString2 = CustomBulletSpan.replaceBulletSpan(spannableString1, context)
    assertThat(getNoofBulletSpans(spannableString1)).isEqualTo(0)
    assertThat(getNoofCustomBulletSpans(spannableString1)).isEqualTo(0)
    assertThat(getNoofBulletSpans(spannableString2)).isEqualTo(0)
    assertThat(getNoofCustomBulletSpans(spannableString2)).isEqualTo(0)
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_spannableStringWithBulletSpanIsNotSame() {
    val spannableString1 = testStringWithBulletSpan
    val spannableString2 = CustomBulletSpan.replaceBulletSpan(spannableString1, context)
//    assertThat(getNoofBulletSpans(spannableString1)).isEqualTo(1) //0
//    assertThat(getNoofCustomBulletSpans(spannableString1)).isEqualTo(0) //1
    assertThat(getNoofBulletSpans(spannableString2)).isEqualTo(0) //  0
    assertThat(getNoofCustomBulletSpans(spannableString2)).isEqualTo(1) //  1
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_multipleBulletSpanAndUnderlineSpan_underlineSpan() {
    val spannableString1 = testStringWithMultipleBulletSpan
    val spannableString2 = CustomBulletSpan.replaceBulletSpan(spannableString1, context)
//    assertThat(getNoofBulletSpans(spannableString1)).isEqualTo(4) //0
//    assertThat(getNoofCustomBulletSpans(spannableString1)).isEqualTo(0) //4
    assertThat(getNoofUnderlineSpans(spannableString1)).isEqualTo(1) // 0
    assertThat(getNoofBulletSpans(spannableString2)).isEqualTo(0) // 0
    assertThat(getNoofCustomBulletSpans(spannableString2)).isEqualTo(4) // 4
    assertThat(getNoofUnderlineSpans(spannableString2)).isEqualTo(1) // 0
  }

  @Test
  fun customBulletSpan_testReplaceBulletSpan_customBulletSpans_RemainsSame() {
    val spannableString1 = testStringWithCustomBulletSpan
    val spannableString2 = CustomBulletSpan.replaceBulletSpan(spannableString1, context)
    assertThat(getNoofBulletSpans(spannableString1)).isEqualTo(0)
    assertThat(getNoofCustomBulletSpans(spannableString1)).isEqualTo(1)
    assertThat(getNoofBulletSpans(spannableString2)).isEqualTo(0)
    assertThat(getNoofCustomBulletSpans(spannableString2)).isEqualTo(1)
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
    val customBulletSpannble = CustomBulletSpan.replaceBulletSpan(spannableString, context)
//    customBulletSpannble.getSpans<CustomBulletSpan>(
//      0,
//      spannableString.length,
//      CustomBulletSpan::class.java
//    ).forEach {
//      val margin = it.getLeadingMargin(true)
//      assertThat(margin).isEqualTo(expectedMargin)
//    }
    val leadingMargin = customBulletSpannble.getSpans(
      0,
      spannableString.length,
      CustomBulletSpan::class.java
    )[0].getLeadingMargin(true)
    assertThat(leadingMargin).isEqualTo(expectedMargin)
//    assertThat(customBulletSpannble.getSpans<CustomBulletSpan>().size).isGreaterThan(0)
  }

  private fun getBulletSpans(spannableString: SpannableStringBuilder): Array<out BulletSpan> {
    return spannableString.getSpans<BulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getCustomBulletSpans(
    spannableString: SpannableStringBuilder
  ): Array<out CustomBulletSpan> {
    return spannableString.getSpans<CustomBulletSpan>(
      0,
      spannableString.length
    )
  }

  private fun getUnderlineSpans(spannableString: SpannableStringBuilder): Array<out UnderlineSpan> {
    return spannableString.getSpans<UnderlineSpan>(
      0,
      spannableString.length
    )
  }

  private fun getNoofBulletSpans(spannableString: SpannableStringBuilder): Int {
    return getBulletSpans(spannableString).size
  }

  private fun getNoofCustomBulletSpans(spannableString: SpannableStringBuilder): Int {
    return getCustomBulletSpans(spannableString).size
  }

  private fun getNoofUnderlineSpans(spannableString: SpannableStringBuilder): Int {
    return getUnderlineSpans(spannableString).size
  }

  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
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
