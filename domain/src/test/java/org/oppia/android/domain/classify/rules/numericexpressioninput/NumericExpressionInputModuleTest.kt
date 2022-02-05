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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.domain.classify.rules.NumericExpressionInputRules
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NumericExpressionInputModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericExpressionInputModuleTest {
  @Inject
  @NumericExpressionInputRules
  lateinit var numericExpressionInputClassifiers: Map<String, @JvmSuppressWildcards RuleClassifier>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_hasAtLeastOneClassifier() {
    assertThat(numericExpressionInputClassifiers).isNotEmpty()
  }

  @Test
  fun testModule_hasNoDuplicateClassifiers() {
    assertThat(numericExpressionInputClassifiers.values.toSet()).hasSize(
      numericExpressionInputClassifiers.size
    )
  }

  @Test
  fun testModule_providesMatchesExactlyWithClassifier() {
    assertThat(numericExpressionInputClassifiers).containsKey("MatchesExactlyWith")
  }

  @Test
  fun testModule_providesMatchesUpToTrivialManipulationsClassifier() {
    assertThat(numericExpressionInputClassifiers).containsKey("MatchesUpToTrivialManipulations")
  }

  @Test
  fun testModule_providesIsEquivalentToClassifier() {
    assertThat(numericExpressionInputClassifiers).containsKey("MatchesExactlyWith")
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumericExpressionInputModuleTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

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
      TestDispatcherModule::class, LoggerModule::class, RobolectricModule::class,
      NumericExpressionInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: NumericExpressionInputModuleTest)
  }
}
