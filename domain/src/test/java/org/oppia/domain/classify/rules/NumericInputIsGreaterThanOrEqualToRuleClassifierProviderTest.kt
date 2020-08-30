package org.oppia.domain.classify.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.DaggerNumericInputEqualsRuleClassifierProviderTest_TestApplicationComponent
import org.oppia.domain.classify.NumericInputEqualsRuleClassifierProviderTest
import org.oppia.domain.classify.rules.numericinput.NumericInputEqualsRuleClassifierProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [NumericInputIsGreaterThanOrEqualTosRuleClassifierProviderTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericInputIsGreaterThanOrEqualTosRuleClassifierProviderTest {
  private val POSITIVE_REAL_VALUE_1_5 = createReal(value = 1.5)
  private val POSITIVE_REAL_VALUE_3_5 = createReal(value = 3.5)
  private val NEGATIVE_REAL_VALUE_1_5 = createReal(value = -1.5)
  private val NEGATIVE_REAL_VALUE_3_5 = createReal(value = -3.5)
  private val STRING_VALUE = createString(value = "test")

  @Inject
  internal lateinit var numericInputEqualsRuleClassifierProvider:
    NumericInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    numericInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }





  private fun createReal(value: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(value).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }


  private fun setUpTestApplicationComponent() {
    DaggerNumericInputIsGreaterThanOrEqualToRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move to a common test library.
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: NumericInputEqualsRuleClassifierProviderTest)
  }
}