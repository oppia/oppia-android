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
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [FractionInputIsExactlyEqualToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputIsExactlyEqualToRuleClassifierProviderTest {
  /**
   * Store fractions in format:
   *    <0 or 1 indicating whether it's negative>, <numerator>, <denominator>
   *    e.g. -1/2 would be {1, 1, 2}
   * Store whole numbers in format:
   *    <0 or 1 indicating whether it's negative>, <whole number>
   *    e.g. 1234 would be {0, 1234}
   * Store mixed numbers in format:
   *    <0 or 1 indicating whether it's negative>, <whole number>, <numerator>, <denominator>
   *    e.g. -1234 1/2 would be {1, 1234, 1, 2}
   */
  private val NON_NEGATIVE_VALUE_0 = createNonNegativeInt(value = 0)
  private val WHOLE_POS_1 = listOf(0, 123)
  private val WHOLE_POS_2 = listOf(0, 321)
  private val FRAC_POS_1 = listOf(0, 2, 4)
  private val FRAC_POS_2 = listOf(0, 1, 2)
  private val FRAC_POS_3 = listOf(0, 123, 1)
  private val MIXED_POS_1 = listOf(0, 123, 1, 2)
  private val MIXED_POS_2 = listOf(0, 123, 1, 3)
  private val MIXED_NEG_1 = listOf(1, 123, 1, 2)

  @Inject
  internal lateinit var fractionInputIsExactlyEqualToRuleClassifierProvider:
    FractionInputIsExactlyEqualToRuleClassifierProvider

  private val isExactlyEqualClassifierProvider: RuleClassifier by lazy {
    fractionInputIsExactlyEqualToRuleClassifierProvider.createRuleClassifier()
  }

  @Test
  fun testAnswer_testFraction_bothWholeNumbers_bothValuesMatch() {
    val inputs = mapOf("f" to createWholeNumberOnly(WHOLE_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createWholeNumberOnly(WHOLE_POS_1),
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testFraction_bothWholeNumbers_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createWholeNumberOnly(WHOLE_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createWholeNumberOnly(WHOLE_POS_2),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_bothFractions_bothValuesMatch() {
    val inputs = mapOf("f" to createFractionOnly(FRAC_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createFractionOnly(FRAC_POS_1),
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testFraction_bothFractions_oneFractionReduced_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createFractionOnly(FRAC_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createFractionOnly(FRAC_POS_2),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_bothMixed_bothValuesMatch() {
    val inputs = mapOf("f" to createMixedNumber(MIXED_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createMixedNumber(MIXED_POS_1),
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testFraction_bothMixed_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createMixedNumber(MIXED_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createMixedNumber(MIXED_POS_2),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_bothMixedDiffSign_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createMixedNumber(MIXED_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createMixedNumber(MIXED_NEG_1),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_wholeAndMixed_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createMixedNumber(MIXED_POS_1))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createWholeNumberOnly(WHOLE_POS_1),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_wholeAndFraction_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createFractionOnly(FRAC_POS_3))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createWholeNumberOnly(WHOLE_POS_1),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testFraction_fractionAndMixed_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to createFractionOnly(FRAC_POS_2))

    val matches =
      isExactlyEqualClassifierProvider.matches(
        answer = createMixedNumber(MIXED_POS_1),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("f" to NON_NEGATIVE_VALUE_0)

    val exception = assertThrows(IllegalStateException::class) {
      isExactlyEqualClassifierProvider.matches(
        answer = createFractionOnly(FRAC_POS_1),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type FRACTION not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testAnswer_testRatio_missingInputF_throwsException() {
    val inputs = mapOf("y" to createFractionOnly(FRAC_POS_1))

    val exception = assertThrows(IllegalStateException::class) {
      isExactlyEqualClassifierProvider.matches(
        answer = createFractionOnly(FRAC_POS_1),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
  }

  private fun createFractionOnly(value: List<Int>): InteractionObject {
    // Fraction-only numbers imply no whole number.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(value[0] == 1)
        .setNumerator(value[1])
        .setDenominator(value[2])
        .build()
    ).build()
  }

  private fun createWholeNumberOnly(value: List<Int>): InteractionObject {
    // Whole number fractions imply '0/1' fractional parts.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(value[0] == 1)
        .setWholeNumber(value[1])
        .setNumerator(0)
        .setDenominator(1)
        .build()
    ).build()
  }

  private fun createMixedNumber(value: List<Int>): InteractionObject {
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(value[0] == 1)
        .setWholeNumber(value[1])
        .setNumerator(value[2])
        .setDenominator(value[3])
        .build()
    ).build()
  }

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputIsExactlyEqualToRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: FractionInputIsExactlyEqualToRuleClassifierProviderTest)
  }
}
