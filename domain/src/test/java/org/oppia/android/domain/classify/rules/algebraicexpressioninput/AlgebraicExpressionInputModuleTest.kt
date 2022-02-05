package org.oppia.android.domain.classify.rules.algebraicexpressioninput

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
import org.oppia.android.domain.classify.rules.AlgebraicExpressionInputRules
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AlgebraicExpressionInputModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AlgebraicExpressionInputModuleTest {
  @field:[Inject AlgebraicExpressionInputRules]
  lateinit var algebraicExpressionInputClassifiers: Map<
    String, @JvmSuppressWildcards RuleClassifier>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_hasAtLeastOneClassifier() {
    assertThat(algebraicExpressionInputClassifiers).isNotEmpty()
  }

  @Test
  fun testModule_hasNoDuplicateClassifiers() {
    assertThat(algebraicExpressionInputClassifiers.values.toSet()).hasSize(
      algebraicExpressionInputClassifiers.size
    )
  }

  @Test
  fun testModule_providesMatchesExactlyWithClassifier() {
    assertThat(algebraicExpressionInputClassifiers).containsKey("MatchesExactlyWith")
  }

  @Test
  fun testModule_providesMatchesUpToTrivialManipulationsClassifier() {
    assertThat(algebraicExpressionInputClassifiers).containsKey("MatchesUpToTrivialManipulations")
  }

  @Test
  fun testModule_providesIsEquivalentToClassifier() {
    assertThat(algebraicExpressionInputClassifiers).containsKey("MatchesExactlyWith")
  }

  private fun setUpTestApplicationComponent() {
    DaggerAlgebraicExpressionInputModuleTest_TestApplicationComponent
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
      AlgebraicExpressionInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: AlgebraicExpressionInputModuleTest)
  }
}
