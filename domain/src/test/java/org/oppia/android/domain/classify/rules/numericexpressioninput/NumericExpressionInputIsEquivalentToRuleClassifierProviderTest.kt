package org.oppia.android.domain.classify.rules.numericexpressioninput

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumericExpressionInputIsEquivalentToRuleClassifierProvider]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericExpressionInputIsEquivalentToRuleClassifierProviderTest {
  @Inject
  internal lateinit var provider: NumericExpressionInputIsEquivalentToRuleClassifierProvider

  private lateinit var classifier: RuleClassifier

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    classifier = provider.createRuleClassifier()
  }

  // TODO: finish tests.

  @Test
  fun test() {
    val answerExpression = createMathExpression("0")
    val inputExpression = createMathExpression("1")

    val matches =
      classifier.matches(
        answerExpression,
        inputs = mapOf("x" to inputExpression),
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumericExpressionInputIsEquivalentToRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  private fun createMathExpression(rawExpression: String) = InteractionObject.newBuilder().apply {
    mathExpression = rawExpression
  }.build()

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestDispatcherModule::class, LoggerModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: NumericExpressionInputIsEquivalentToRuleClassifierProviderTest)
  }
}
