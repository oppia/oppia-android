package org.oppia.android.domain.classify.rules.fractioninput

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

/** Tests for [FractionInputHasNumeratorEqualToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputHasNumeratorEqualToRuleClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_TEST_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(
      value = 0
    )
  private val WHOLE_NUMBER_VALUE_TEST_123 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = false,
      value = 123
    )
  private val FRACTION_VALUE_TEST_2_OVER_4 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 2,
      denominator = 4
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_2_OVER_4 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = -2,
      denominator = 4
    )
  private val SIGNED_INT_VALUE_TEST_1 =
    InteractionObjectTestBuilder.createSignedInt(
      value = 1
    )
  private val SIGNED_INT_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createSignedInt(
      value = 2
    )
  private val SIGNED_INT_NEGATIVE_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createSignedInt(
      value = -2
    )

  @Inject
  internal lateinit var fractionInputHasNumeratorEqualToRuleClassifierProvider:
    FractionInputHasNumeratorEqualToRuleClassifierProvider

  private val numeratorIsEqualClassifierProvider: RuleClassifier by lazy {
    fractionInputHasNumeratorEqualToRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testNumeratorEquals_negativeNumerators_bothValuesMatch() {
    val inputs = mapOf("x" to SIGNED_INT_NEGATIVE_VALUE_TEST_2)

    val matches =
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_NEGATIVE_2_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNumeratorEquals_wholeNumber123Answer_withSignedInt1Input_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to SIGNED_INT_VALUE_TEST_1)

    val matches =
      numeratorIsEqualClassifierProvider.matches(
        answer = WHOLE_NUMBER_VALUE_TEST_123,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNumeratorEquals_fraction2Over4Answer_withSignedInt1Input_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to SIGNED_INT_VALUE_TEST_1)

    val matches =
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_2_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNumeratorEquals_fraction2Over4Answer_withSignedIntNegative2Input_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to SIGNED_INT_NEGATIVE_VALUE_TEST_2)

    val matches =
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_2_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNumeratorEquals_fraction2Over4Answer_withSignedInt2Input_bothValuesMatch() {
    val inputs = mapOf("x" to SIGNED_INT_VALUE_TEST_2)

    val matches =
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_2_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNumeratorEquals_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_2_OVER_4,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type SIGNED_INT not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testNumeratorEquals_missingInputF_throwsException() {
    val inputs = mapOf("y" to FRACTION_VALUE_TEST_2_OVER_4)

    val exception = assertThrows(IllegalStateException::class) {
      numeratorIsEqualClassifierProvider.matches(
        answer = FRACTION_VALUE_TEST_2_OVER_4,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputHasNumeratorEqualToRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: FractionInputHasNumeratorEqualToRuleClassifierProviderTest)
  }
}
