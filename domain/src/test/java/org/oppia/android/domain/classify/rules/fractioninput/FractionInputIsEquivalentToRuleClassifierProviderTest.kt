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

  private val WHOLE_NUMBER_VALUE_123 =
    InteractionObjectTestBuilder.createWholeNumber(isNegative = false, value = 123)
  private val WHOLE_NUMBER_VALUE_254 =
    InteractionObjectTestBuilder.createWholeNumber(isNegative = false, value = 254)
  private val FRACTION_VALUE_2_OVER_8 =
    InteractionObjectTestBuilder.createFraction(isNegative = false, numerator = 2, denominator = 8)
  private val FRACTION_VALUE_1_OVER_5 =
    InteractionObjectTestBuilder.createFraction(isNegative = false, numerator = 1, denominator = 5)
  private val FRACTION_VALUE_33_OVER_2 = InteractionObjectTestBuilder.createFraction(
    isNegative = false,
    numerator = 33,
    denominator = 2
  )
  private val FRACTION_VALUE_242_OVER_1 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 242,
      denominator = 1
    )
  private val MIXED_NUMBER_VALUE_6_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 6,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_55_1_OVER_4 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 55,
      numerator = 1,
      denominator = 4
    )
  private val NON_NEGATIVE_VALUE_679_2_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 679,
      numerator = 2,
      denominator = 3
    )

  @Inject
  internal lateinit var fractionInputIsEquivalentToRuleClassifierProvider:
    FractionInputIsEquivalentToRuleClassifierProvider

  private val inputIsEquivalentToRuleClassifier by lazy {
    fractionInputIsEquivalentToRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testEquality_wholeNumber123Answer_withWholeNumber123Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to WHOLE_NUMBER_VALUE_123)
    val answer = WHOLE_NUMBER_VALUE_123

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = answer, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_wholeNumber123Answer_withWholeNumber254Input_bothValuesNotEquivalent() {
    val inputs = mapOf("f" to WHOLE_NUMBER_VALUE_254)
    val answer = WHOLE_NUMBER_VALUE_123

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = WHOLE_NUMBER_VALUE_123, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_fraction2Over4Answer_withFraction2Over4Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to FRACTION_VALUE_2_OVER_8)
    val answer = FRACTION_VALUE_2_OVER_8

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = FRACTION_VALUE_2_OVER_8, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_fraction2Over4Answer_withFraction1Over5Input_bothValuesNotEquivalent() {
    val inputs = mapOf("f" to FRACTION_VALUE_1_OVER_5)
    val answer = FRACTION_VALUE_2_OVER_8

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = FRACTION_VALUE_2_OVER_8, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_fraction2Over133Answer_withFaction1Over242Input_bothValuesEquivalent() {
    val inputs = mapOf("f" to FRACTION_VALUE_33_OVER_2)
    val answer = FRACTION_VALUE_33_OVER_2

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = FRACTION_VALUE_33_OVER_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_fraction2Over133Answer_withFaction1Over242Input_bothValuesNotEquivalent() {
    val inpus = mapOf("f" to FRACTION_VALUE_242_OVER_1)
    val answer = FRACTION_VALUE_33_OVER_2

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = FRACTION_VALUE_33_OVER_2, inputs = inpus)

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_mixedNumbr6And1Over2Answer_withMixedNumber6And1Over2Input_bothValuesMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_6_1_OVER_2)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = MIXED_NUMBER_VALUE_6_1_OVER_2,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquality_mixedNumber55And1Over4Answer_withMixedNum6And1Over2Input_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_6_1_OVER_2)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = MIXED_NUMBER_VALUE_55_1_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_wholeNumberAnswer_withMixedNumberInput_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_6_1_OVER_2)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(answer = WHOLE_NUMBER_VALUE_254, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquality_negativeMixedNumberAnswer_withPositveMixedNumberInput_bothValuesDoNotMatche() {
    val inputs = mapOf("f" to NON_NEGATIVE_VALUE_679_2_OVER_3)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = MIXED_NUMBER_VALUE_55_1_OVER_4,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEqualityOf_mixedNumberAnswer_withFractionInput_bothValuesDoNotMatch() {
    val inputs = mapOf("f" to FRACTION_VALUE_1_OVER_5)

    val matches =
      inputIsEquivalentToRuleClassifier.matches(
        answer = MIXED_NUMBER_VALUE_6_1_OVER_2,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEqualuality_missingInputF_throwsException() {
    val inputs = mapOf("y" to FRACTION_VALUE_2_OVER_8)

    val exception = assertThrows(IllegalStateException::class) {
      inputIsEquivalentToRuleClassifier.matches(answer = FRACTION_VALUE_2_OVER_8, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
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
