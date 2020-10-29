package org.oppia.android.domain.classify.rules.ratioinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.oppia.android.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [RatioInputEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioInputEqualsRuleClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_TEST_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(
      value = 0
    )
  private val RATIO_VALUE_TEST_1_2_3 =
    InteractionObjectTestBuilder.createRatio(
      listOf(1, 2, 3)
    )
  private val RATIO_VALUE_TEST_2_4_6 =
    InteractionObjectTestBuilder.createRatio(
      listOf(2, 4, 6)
    )
  private val RATIO_VALUE_TEST_2_4_6_8 =
    InteractionObjectTestBuilder.createRatio(
      listOf(2, 4, 6, 8)
    )

  @Inject
  internal lateinit var ratioInputEqualsRuleClassifierProvider:
    RatioInputEqualsRuleClassifierProvider

  private val equalsClassifierProvider: RuleClassifier by lazy {
    ratioInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_testRatio_ratioSimplified_bothValuesMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_1_2_3)

    val matches =
      equalsClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testRatio_ratioNonSimplified_ratioSimplified_bothValuesDoNoMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6)

    val matches =
      equalsClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testRatio_ratio4Terms_ratio3Terms_bothValuesDoNoMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6_8)

    val matches =
      equalsClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      equalsClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type RATIO_EXPRESSION not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testAnswer_testRatio_missingInputX_throwsException() {
    val inputs = mapOf("y" to RATIO_VALUE_TEST_1_2_3)

    val exception = assertThrows(IllegalStateException::class) {
      equalsClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerRatioInputEqualsRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(test: RatioInputEqualsRuleClassifierProviderTest)
  }
}
