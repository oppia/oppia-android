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

/** Tests for [FractionInputHasNoFractionalPartRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputHasNoFractionalPartRuleClassifierProviderTest {

  private val WHOLE_NUMBER_123 = createWholeNumber(isNegative = false, value = 123)
  private val FRACTION_2_OVER_4 = createFraction(isNegative = false, numerator = 2, denominator = 4)
  private val FRACTION_1_OVER_2 = createFraction(isNegative = false, numerator = 1, denominator = 2)
  private val FRACTION_20_OVER_5 = createFraction(isNegative = false, numerator = 20, denominator = 5)
  private val MIXED_NUMBER_123_1_OVER_2 =
    createMixedNumber(isNegative = false, wholeNumber = 123, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_123_0_OVER_3 =
    createMixedNumber(isNegative = false, wholeNumber = 123, numerator = 0, denominator = 3)
  private val MIXED_NUMBER_NEGATIVE_123_1_OVER_2 =
    createMixedNumber(isNegative = true, wholeNumber = 123, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_NEGATIVE_123_0_OVER_3 =
    createMixedNumber(isNegative = true, wholeNumber = 123, numerator = 0, denominator = 3)

  @Inject
  internal lateinit var fractionInputHasNoFractionalPartRuleClassifierProvider:
    FractionInputHasNoFractionalPartRuleClassifierProvider

  private val hasNoFractionalPartClassifierProvider: RuleClassifier by lazy {
    fractionInputHasNoFractionalPartRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testNoFractionalPart_wholeNumber123Answer_hasNoFractionalPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = WHOLE_NUMBER_123,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNoFractionPart_fraction2Over4Answer_hasFractionalPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = FRACTION_2_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNoFractionPart_mixedNumber123And1Over2Answer_hasFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = MIXED_NUMBER_123_1_OVER_2,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNoFractionPart_mixedNumber123And0Over3Answer_hasNoFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = MIXED_NUMBER_123_0_OVER_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNoFractionPart_mixedNumberNegative123And1Over2Answer_hasFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = MIXED_NUMBER_NEGATIVE_123_1_OVER_2,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }


  @Test
  fun testNoFractionPart_mixedNumberNegative123And0Over3Answer_hasNoFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = MIXED_NUMBER_NEGATIVE_123_0_OVER_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNoFractionPart_fraction1Over2Answer_hasFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = FRACTION_1_OVER_2,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNoFractionPart_fraction20Over5Answer_hasNoFractionPart() {
    val inputs: Map<String, InteractionObject> = mapOf()

    val matches =
      hasNoFractionalPartClassifierProvider.matches(
        answer = FRACTION_20_OVER_5,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  private fun createFraction(
    isNegative: Boolean,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    // Fraction-only numbers imply no whole number.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  private fun createWholeNumber(isNegative: Boolean, value: Int): InteractionObject {
    // Whole number fractions imply '0/1' fractional parts.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(value)
        .setNumerator(0)
        .setDenominator(1)
        .build()
    ).build()
  }

  private fun createMixedNumber(
    isNegative: Boolean,
    wholeNumber: Int,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(wholeNumber)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputHasNoFractionalPartRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: FractionInputHasNoFractionalPartRuleClassifierProviderTest)
  }
}
