package org.oppia.android.domain.classify.rules.numberwithunits

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

/** Tests for [NumberWithUnitsIsEqualToRuleClassifierProviderTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumberWithUnitsIsEqualToRuleClassifierProviderTest {

  private val TEST_WHOLE_NUMBER_VALUE_9 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = false, value = 9
    )
  private val TEST_NUMBER_FRACTION_VALUE_2_OVER_5 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 5
    ).fraction
  private val TEST_NUMBER_FRACTION_VALUE_1_OVER_4 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 1, denominator = 4
    ).fraction
  private val TEST_REAL_VALUE_DIFFERENT_TYPE =
    InteractionObjectTestBuilder.createReal(value = 6.9)
  private val TEST_REAL_VALUE =
    InteractionObjectTestBuilder.createReal(value = 3.8).real
  private val TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "p", exponent = 5)
  private val TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "e", exponent = 3)
  private val TEST_NUMBER_WITH_UNITS_ANSWER =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      TEST_NUMBER_FRACTION_VALUE_2_OVER_5,
      listOf(
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3,
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5
      )
    )
  private val TEST_NUMBER_WITH_UNITS_INPUT =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      TEST_NUMBER_FRACTION_VALUE_2_OVER_5,
      listOf(
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3,
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5
      )
    )
  private val TEST_DIFFERENT_NUMBERS_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      TEST_NUMBER_FRACTION_VALUE_1_OVER_4,
      listOf(
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5,
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3
      )
    )
  private val TEST_REAL_INPUT_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      TEST_REAL_VALUE,
      listOf(
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3,
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5
      )
    )
  private val TEST_REAL_ANSWER_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      TEST_REAL_VALUE,
      listOf(
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_5,
        TEST_NUMBER_UNIT_WITH_STRING_TO_POWER_3
      )
    )

  @Inject
  internal lateinit var numberWithUnitsIsEqualToRuleClassifierProvider:
    NumberWithUnitsIsEqualToRuleClassifierProvider

  private val unitsIsEqualsRuleClassifier by lazy {
    numberWithUnitsIsEqualToRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testEquals_fractionInputNumberWithUnits_withFractionAnswerWithUnits_bothValuesMatches() {
    val inputs = mapOf("f" to TEST_NUMBER_WITH_UNITS_INPUT)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_NUMBER_WITH_UNITS_ANSWER,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_differentFractionWithUnits_withRealAnswerNumberWithUnits_bothDoNotMatch() {
    val inputs = mapOf("f" to TEST_DIFFERENT_NUMBERS_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_REAL_INPUT_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_fractionNumberWithUnits_withFractionNumberWithUnits_bothValuesMatches() {
    val inputs = mapOf("f" to TEST_NUMBER_WITH_UNITS_INPUT)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_NUMBER_WITH_UNITS_INPUT,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEqual_fractionInputNumberWithUnits_withSameFractionInputNumberWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to TEST_NUMBER_WITH_UNITS_ANSWER)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_NUMBER_WITH_UNITS_ANSWER,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_differentFractionWithUnitsInput_withFractionAnswerWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to TEST_DIFFERENT_NUMBERS_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_NUMBER_WITH_UNITS_ANSWER,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_inputRealNumbersWithUnits_withRealAnswerNumberWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to TEST_REAL_INPUT_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_REAL_ANSWER_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_wholeNumberInputValue_withRealAnswerNumWithUnits_throwsException() {
    val inputs = mapOf("f" to TEST_WHOLE_NUMBER_VALUE_9)

    val exception = assertThrows(IllegalStateException::class) {

      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_REAL_VALUE_DIFFERENT_TYPE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected answer to be of type NUMBER_WITH_UNITS not REAL"
      )
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumberWithUnitsIsEqualToRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }

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

    fun inject(test: NumberWithUnitsIsEqualToRuleClassifierProviderTest)
  }
}
