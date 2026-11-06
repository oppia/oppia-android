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
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.player.state.testing.InteractionObjectTestBuilder
import org.oppia.android.domain.classify.ClassificationContext
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
  private val FRACTION_VALUE_TEST_2_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 3
    ).fraction
  private val FRACTION_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_6_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 6, denominator = 3
    ).fraction
  private val FRACTION_VALUE_TEST_20_OVER_30 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 20, denominator = 30
    ).fraction
  private val FRACTION_VALUE_TEST_100 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 100, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_200 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 200, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_2000 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2000, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_20 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 20, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_10 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 10, denominator = 1
    ).fraction
  private val DOUBLE_VALUE_TEST_DIFFERENT_TYPE =
    InteractionObjectTestBuilder.createReal(value = 2.5)
  private val DOUBLE_VALUE_TEST =
    InteractionObjectTestBuilder.createReal(value = 2.6).real
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = 2)
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "s", exponent = 1)
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_3 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "kg", exponent = 3)
  private val UNIT_KG_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "kg", exponent = 1)
  private val UNIT_G_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "g", exponent = 1)
  private val UNIT_M_TO_POWER_NEGATIVE_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = -2)
  private val UNIT_M_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = 1)
  private val UNIT_M_TO_POWER_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = 2)
  private val UNIT_METER_TO_POWER_NEGATIVE_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "meter", exponent = -2)
  private val UNIT_CM_TO_POWER_NEGATIVE_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "cm", exponent = -2)
  private val UNIT_RUPEES_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "rupees", exponent = 1)
  private val UNIT_RUPEE_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "rupee", exponent = 1)
  private val UNIT_PAISE_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "paise", exponent = 1)
  private val UNIT_DOLLARS_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "dollars", exponent = 1)
  private val UNIT_DOLLAR_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "dollar", exponent = 1)
  private val UNIT_DOLLARS_TITLE_CASE_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "Dollars", exponent = 1)
  private val UNIT_CENTS_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "cents", exponent = 1)
  private val UNIT_S_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "s", exponent = 1)
  private val UNIT_S_TO_POWER_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "s", exponent = 2)
  private val UNIT_S_TO_POWER_NEGATIVE_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "s", exponent = -1)
  private val UNIT_N_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "N", exponent = 1)
  private val UNIT_J_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "J", exponent = 1)
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
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testFractionInputNumWithUnits_testFractionAnsNumWithUnits_inDiffOrder_bothValuesMatch() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testFractionInputNumWithUnits_testFractionAnsNumWithUnits_inSameOrder_bothValuesMatch() {
    val inputs = mapOf("f" to ANSWER_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_withDiffUnits_bothDoNotMatch() {
    val inputs = mapOf("f" to INPUT_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = DIFF_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_inDiffOrder_bothValuesMatch() {
    val inputs = mapOf("f" to INPUT_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_REAL_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testRealInputNumberWithUnits_testRealAnsNumberWithUnits_inSameOrder_bothValuesMatch() {
    val inputs = mapOf("f" to ANSWER_TEST_REAL_NUMBER_WITH_UNITS)

    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_REAL_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testApproximateValueMatch_fractionSixOverThreeVsTwo_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_6_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testApproximateValueMatch_fractionTwentyOverThirtyVsTwoThirds_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_20_OVER_30,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testApproximateValueMatch_realVsFractionEquivalent_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createRealNumberWithUnits(
        0.6666666666666666,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testApproximateValueMatch_realPoint66VsFractionTwoThirds_doesNotMatch() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createRealNumberWithUnits(
        0.66,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = false
    )
  }

  @Test
  fun testValueMismatch_realTwoPointFiveVsTwo_doesNotMatch() {
    assertEquivalent(
      answer = createRealNumberWithUnits(
        2.0,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createRealNumberWithUnits(
        2.5,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = false
    )
  }

  @Test
  fun testExponentMismatch_kgM2VsKgMNegative2_doesNotMatch() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_2)
      ),
      expected = false
    )
  }

  @Test
  fun testUnitMismatch_hundredRupeesVsHundredPaise_doesNotMatch() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_PAISE_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      ),
      expected = false
    )
  }

  @Test
  fun testUnitMismatch_hundredDollarsVsHundredCents_doesNotMatch() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_CENTS_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      ),
      expected = false
    )
  }

  @Test
  fun testUnitMismatch_hundredRupeesVsHundredDollars_doesNotMatch() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      ),
      expected = false
    )
  }

  @Test
  fun testUnitSynonym_twoKgMeterNegative2VsTwoKgMNegative2_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_METER_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitSynonym_hundredRupeeVsHundredRupees_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEE_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitSynonym_hundredDollarVsHundredDollars_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLAR_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLARS_TITLE_CASE_TO_POWER_1)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitConversion_twoThousandGmNegative2VsTwoKgMNegative2_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2000,
        listOf(UNIT_G_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitConversion_realPoint2GcmNegative2VsTwoKgMNegative2_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createRealNumberWithUnits(
        0.2,
        listOf(UNIT_G_TO_POWER_1, UNIT_CM_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitConversion_fractionTwoOverThirtyGcmNegative2VsTwoThirdsKgMNegative2_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      ),
      input = createFractionNumberWithUnits(
        InteractionObjectTestBuilder.createFraction(
          isNegative = false, numerator = 2, denominator = 30
        ).fraction,
        listOf(UNIT_G_TO_POWER_1, UNIT_CM_TO_POWER_NEGATIVE_2)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitConversion_twoRupeesVsTwoHundredPaise_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_200,
        listOf(UNIT_PAISE_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_RUPEES_TO_POWER_1)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitConversion_twoDollarsVsTwoHundredCents_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_200,
        listOf(UNIT_CENTS_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitReduction_twentyMSquareSecondPerSecondVsTwentyMPerSecond_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_20,
        listOf(UNIT_M_TO_POWER_1, UNIT_S_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_20,
        listOf(UNIT_M_TO_POWER_1, UNIT_S_TO_POWER_2, UNIT_S_TO_POWER_NEGATIVE_1)
      ),
      expected = true
    )
  }

  @Test
  fun testUnitCombination_tenNewtonMeterVsTenJoule_matches() {
    assertEquivalent(
      answer = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_10,
        listOf(UNIT_J_TO_POWER_1)
      ),
      input = createFractionNumberWithUnits(
        FRACTION_VALUE_TEST_10,
        listOf(UNIT_N_TO_POWER_1, UNIT_M_TO_POWER_1)
      ),
      expected = true
    )
  }

  @Test
  fun testInputNumberWithUnits_testAnswer_inputWithIncorrectType_verifyThrowsException() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val exception = assertThrows<IllegalStateException>() {
      unitIsEquivalentRuleClassifier.matches(
        answer = DOUBLE_VALUE_TEST_DIFFERENT_TYPE,
        inputs = inputs,
        classificationContext = ClassificationContext()
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

    val exception = assertThrows<IllegalStateException>() {
      unitIsEquivalentRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected classifier inputs to contain parameter with name 'f' but had: [x]"
      )
  }

  private fun createFractionNumberWithUnits(
    number: org.oppia.android.app.model.Fraction,
    units: List<org.oppia.android.app.model.NumberUnit>
  ): InteractionObject {
    return InteractionObjectTestBuilder.createNumberWithUnitsForFraction(number, units)
  }

  private fun createRealNumberWithUnits(
    number: Double,
    units: List<org.oppia.android.app.model.NumberUnit>
  ): InteractionObject {
    return InteractionObjectTestBuilder.createNumberWithUnitsForReal(number, units)
  }

  private fun assertEquivalent(
    answer: InteractionObject,
    input: InteractionObject,
    expected: Boolean
  ) {
    val matches =
      unitIsEquivalentRuleClassifier.matches(
        answer = answer,
        inputs = mapOf("f" to input),
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isEqualTo(expected)
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
