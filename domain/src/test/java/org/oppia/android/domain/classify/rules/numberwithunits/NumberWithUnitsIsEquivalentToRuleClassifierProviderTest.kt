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
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NumberWithUnitsIsEquivalentToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumberWithUnitsIsEquivalentToRuleClassifierProviderTest {

  private val FRACTION_VALUE_TEST_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 1, denominator = 2
    ).fraction
  private val DOUBLE_VALUE_TEST_DIFFERENT_TYPE =
    InteractionObjectTestBuilder.createReal(value = 2.5)
  private val DOUBLE_VALUE_TEST =
    InteractionObjectTestBuilder.createReal(value = 2.6).real
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "a", exponent = 2)
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "b", exponent = 1)
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_3 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "c", exponent = 3)
  private val ANSWER_TEST_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_1_OVER_2,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_1,
        NUMBER_UNIT_TEST_STRING_TO_POWER_2
      )
    )
  private val INPUT_TEST_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_1_OVER_2,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_2,
        NUMBER_UNIT_TEST_STRING_TO_POWER_1
      )
    )
  private val DIFF_TEST_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_1_OVER_2,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_3,
        NUMBER_UNIT_TEST_STRING_TO_POWER_2
      )
    )
  private val ANSWER_TEST_REAL_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      DOUBLE_VALUE_TEST,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_1,
        NUMBER_UNIT_TEST_STRING_TO_POWER_2
      )
    )
  private val INPUT_TEST_REAL_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      DOUBLE_VALUE_TEST,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_2,
        NUMBER_UNIT_TEST_STRING_TO_POWER_1
      )
    )

  @Inject
  internal lateinit var numberWithUnitsIsEquivalentToRuleClassifierProvider:
    NumberWithUnitsIsEquivalentToRuleClassifierProvider

  private val unitIsEquivalentRuleClassifier by lazy {
    numberWithUnitsIsEquivalentToRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFractionInputNumWithUnits_testFractionAnsNumWithUnits_withDiffUnits_bothDoNotMatch() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = DIFF_TEST_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testFractionInputNumWithUnits_testFractionAnsNumWithUnits_inDiffOrder_bothValuesMatch() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testFractionInputNumWithUnits_testFractionAnsNumWithUnits_inSameOrder_bothValuesMatch() {
    val inputs = mapOf("f" to ANSWER_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_withDiffUnits_bothDoNotMatch() {
    val inputs = mapOf("f" to INPUT_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = DIFF_TEST_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_inDiffOrder_bothValuesMatch() {
    val inputs = mapOf("f" to INPUT_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_REAL_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_inSameOrder_bothValuesMatch() {
    val inputs = mapOf("f" to ANSWER_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_REAL_NUMBER_WITH_UNITS,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testInputNumberWithUnits_testAnswer_inputWithIncorrectType_verifyThrowsException() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val exception = assertThrows(IllegalStateException::class) {
      unitIsEquivalentRuleClassifier.matches(
        answer = DOUBLE_VALUE_TEST_DIFFERENT_TYPE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected answer to be of type NUMBER_WITH_UNITS not REAL"
      )
  }

  @Test
  fun testInputNumberWithUnits_testAnswerNumberWithUnits_withXMapping_verifyThrowsException() {
    val inputs = mapOf("x" to INPUT_TEST_NUMBER_WITH_UNITS)

    val exception = assertThrows(IllegalStateException::class) {
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected classifier inputs to contain parameter with name 'f' but had: [x]"
      )
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumberWithUnitsIsEquivalentToRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    fun inject(test: NumberWithUnitsIsEquivalentToRuleClassifierProviderTest)
  }
}
