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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputIsEquivalentToRuleClassifierProviderTest {
  private val WHOLE_NUMBER_123 = createWholeNumber(isNegative = false, value = 123)
  private val WHOLE_NUMBER_254 = createWholeNumber(isNegative = false, value = 254)
  private val FRACTION_2_OVER_8 =
    createFraction(isNegative = false, numerator = 2, denominator = 8)
  private val FRACTION_1_OVER_5 =
    createFraction(isNegative = false, numerator = 1, denominator = 5)
  private val FRACTION_133_OVER_2 =
    createFraction(isNegative = false, numerator = 133, denominator = 2)
  private val FRACTION_242_OVER_1 =
    createFraction(isNegative = false, numerator = 242, denominator = 1)
  private val MIXED_NUMBER_106_1_OVER_2 =
    createMixedNumber(isNegative = false, wholeNumber = 106, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_255_1_OVER_4 =
    createMixedNumber(isNegative = false, wholeNumber = 255, numerator = 1, denominator = 4)

  @Inject
  internal lateinit var fractionInputIsEquivalentToRuleClassifierProvider:
    FractionInputIsEquivalentToRuleClassifierProvider

  private val inputIsEquivalentToRuleClassifier by lazy {
    fractionInputIsEquivalentToRuleClassifierProvider.createRuleClassifier()
  }

  @Test
  fun testEquality_wholeNumber123Answer_withWholeNumber123Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to WHOLE_NUMBER_123)
    val answer = WHOLE_NUMBER_123

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = answer,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_wholeNumber123Answer_withWholeNumber254Input_bothValuesNotEquivalent() {
    val inputs = mapOf("f" to WHOLE_NUMBER_254)
    val answer = WHOLE_NUMBER_123

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = WHOLE_NUMBER_123,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_fraction2Over4Answer_withFraction2Over4Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to FRACTION_2_OVER_8)
    val answer = FRACTION_2_OVER_8

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = FRACTION_2_OVER_8,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_fraction2Over4Answer_withFraction1Over5Input_bothValuesNotEquivalent() {
    val inputs = mapOf("f" to FRACTION_1_OVER_5)
    val answer = FRACTION_2_OVER_8

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = FRACTION_2_OVER_8,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_fraction2Over133Answer_withFaction1Over242Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to FRACTION_133_OVER_2)
    val answer = FRACTION_133_OVER_2

    val matches = inputIsEquivalentToRuleClassifier.matches(
      answer = FRACTION_133_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_fraction2Over133Answer_withFaction1Over242Input_bothValuesNotEquivalent() {
    val inpus = mapOf("f" to FRACTION_242_OVER_1)
    val answer = FRACTION_133_OVER_2

    val matches = inputIsEquivalentToRuleClassifier.matches(
      answer = FRACTION_133_OVER_2,
      inputs = inpus

    )
    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_mixedNumbr106And1Over2Answer_withMixedNumber106And1Over2Input_bothValuesMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_106_1_OVER_2)

    val matches = inputIsEquivalentToRuleClassifier.matches(
      answer = MIXED_NUMBER_106_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_mixedNumber255And1Over4Answer_withMixedNum106And1Over2Input_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_106_1_OVER_2)

    val matches = inputIsEquivalentToRuleClassifier.matches(
      answer = MIXED_NUMBER_255_1_OVER_4,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_wholeNumberAnswer_withMixedNumberInput_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_106_1_OVER_2)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = WHOLE_NUMBER_254,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_missingInputF_throwsException() {
    val inputs = mapOf("y" to FRACTION_2_OVER_8)

    val exception = assertThrows(IllegalStateException::class) {
      inputIsEquivalentToRuleClassifier.matches(
        answer = FRACTION_2_OVER_8,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
  }

  @Test
  fun testEquals_mixedNumberAnswer_withFractionInput_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to FRACTION_1_OVER_5)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = MIXED_NUMBER_106_1_OVER_2,
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

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputIsEquivalentToRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

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

  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: FractionInputIsEquivalentToRuleClassifierProviderTest)
  }
}
