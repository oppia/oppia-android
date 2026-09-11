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
import org.oppia.android.app.player.state.testing.InteractionObjectTestBuilder
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NumberWithUnitsIsEqualToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumberWithUnitsIsEqualToRuleClassifierProviderTest {

  private val WHOLE_NUMBER_VALUE_9 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = false, value = 9
    )
  private val FRACTION_VALUE_TEST_2_OVER_5 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 5
    ).fraction
  private val FRACTION_VALUE_TEST_1_OVER_4 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 1, denominator = 4
    ).fraction
  private val DOUBLE_VALUE_TEST_DIFFERENT_TYPE =
    InteractionObjectTestBuilder.createReal(value = 6.9)
  private val DOUBLE_VALUE_TEST =
    InteractionObjectTestBuilder.createReal(value = 3.8).real
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_5 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "p", exponent = 5)
  private val NUMBER_UNIT_TEST_STRING_TO_POWER_3 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "e", exponent = 3)
  private val NUMBER_UNIT_METER_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "meter", exponent = 1)
  private val NUMBER_UNIT_METER_TO_POWER_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "meter", exponent = 2)
  private val ANSWER_TEST_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_2_OVER_5,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_3,
        NUMBER_UNIT_TEST_STRING_TO_POWER_5
      )
    )
  private val INPUT_TEST_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_2_OVER_5,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_3,
        NUMBER_UNIT_TEST_STRING_TO_POWER_5
      )
    )
  private val TEST_DIFFERENT_NUMBERS_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_1_OVER_4,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_3,
        NUMBER_UNIT_TEST_STRING_TO_POWER_3
      )
    )
  private val TEST_REAL_INPUT_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      DOUBLE_VALUE_TEST,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_3,
        NUMBER_UNIT_TEST_STRING_TO_POWER_5
      )
    )
  private val TEST_REAL_ANSWER_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForReal(
      DOUBLE_VALUE_TEST,
      listOf(
        NUMBER_UNIT_TEST_STRING_TO_POWER_5,
        NUMBER_UNIT_TEST_STRING_TO_POWER_3
      )
    )
  private val TEST_AGGREGATED_FRACTION_ANSWER_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_2_OVER_5,
      listOf(NUMBER_UNIT_METER_TO_POWER_2)
    )
  private val TEST_DUPLICATED_FRACTION_INPUT_NUMBER_WITH_UNITS =
    InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
      FRACTION_VALUE_TEST_2_OVER_5,
      listOf(NUMBER_UNIT_METER_TO_POWER_1, NUMBER_UNIT_METER_TO_POWER_1)
    )
  private val FRACTION_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_6_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 6, denominator = 3
    ).fraction
  private val FRACTION_VALUE_TEST_2_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 3
    ).fraction
  private val FRACTION_VALUE_TEST_20_OVER_30 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 20, denominator = 30
    ).fraction
  private val FRACTION_VALUE_TEST_100 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 100, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_2000 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2000, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_200 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 200, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_2_OVER_30 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 2, denominator = 30
    ).fraction
  private val FRACTION_VALUE_TEST_20 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 20, denominator = 1
    ).fraction
  private val FRACTION_VALUE_TEST_10 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false, numerator = 10, denominator = 1
    ).fraction
  private val UNIT_KG_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "kg", exponent = 1)
  private val UNIT_G_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "g", exponent = 1)
  private val UNIT_M_TO_POWER_NEGATIVE_2 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = -2)
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
  private val UNIT_M_TO_POWER_1 =
    InteractionObjectTestBuilder.createNumberUnit(unit = "m", exponent = 1)
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
  fun testFractionInputNumberWithUnits_withFractionAnswerWithUnits_bothValuesMatches() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testDifferentFractionWithUnits_withRealAnswerNumberWithUnits_bothDoNotMatch() {
    val inputs = mapOf("f" to TEST_DIFFERENT_NUMBERS_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_REAL_INPUT_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testFractionNumberWithUnits_withFractionNumberWithUnits_bothValuesMatches() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = INPUT_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testFractionInputNumberWithUnits_withSameFractionInputNumberWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to ANSWER_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testDifferentFractionWithUnitsInput_withFractionAnswerWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to TEST_DIFFERENT_NUMBERS_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = ANSWER_TEST_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testInputRealNumbersWithUnits_withRealAnswerNumberWithUnits_bothValueMatch() {
    val inputs = mapOf("f" to TEST_REAL_INPUT_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_REAL_ANSWER_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testFractionInputWithDuplicateUnits_withEquivalentAggregatedFractionAnswer_doesNotMatch() {
    val inputs = mapOf("f" to TEST_DUPLICATED_FRACTION_INPUT_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = TEST_AGGREGATED_FRACTION_ANSWER_NUMBER_WITH_UNITS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testFractionInputNumberWithUnits_withEquivalentRealAnswer_doesNotMatch() {
    val inputs = mapOf("f" to INPUT_TEST_NUMBER_WITH_UNITS)

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForReal(
          number = 0.4,
          units = listOf(NUMBER_UNIT_TEST_STRING_TO_POWER_3, NUMBER_UNIT_TEST_STRING_TO_POWER_5)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testExactValueMatch_fractionKgMNegative2_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testExactValueMatch_negativeRealM2_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForReal(
        number = -4.5,
        units = listOf(UNIT_M_TO_POWER_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForReal(
          number = -4.5,
          units = listOf(UNIT_M_TO_POWER_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testExactValueMatch_fractionTwoThirdsKgMNegative2_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2_OVER_3,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testExactValueMatch_hundredRupees_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_RUPEES_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testExactValueMatch_hundredDollars_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_DOLLARS_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testExactValueMatch_hundredCents_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_CENTS_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_CENTS_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testApproximateValueMatch_fractionSixOverThree_vsTwo_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_6_OVER_3,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testApproximateValueMatch_fractionTwentyOverThirty_vsTwoThirds_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_20_OVER_30,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2_OVER_3,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testApproximateValueMatch_realVsFractionEquivalent_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForReal(
        number = 0.666666666666,
        units = listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2_OVER_3,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testApproximateValueMatch_realPoint66VsFractionTwoThirds_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForReal(
        number = 0.66,
        units = listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2_OVER_3,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testValueMismatch_realTwoPointFiveVsTwo_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForReal(
        number = 2.5,
        units = listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForReal(
          number = 2.0,
          units = listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testExponentMismatch_kgM2VsKgMNegative2_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitMismatch_kgM2VsKgMNegative2_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitMismatch_hundredRupeesVsHundredPaise_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_PAISE_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitMismatch_hundredDollarsVsHundredCents_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_CENTS_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitMismatch_hundredRupeesVsHundredDollars_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_100,
        listOf(UNIT_RUPEES_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_100,
          listOf(UNIT_DOLLARS_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitConversion_twoThousandGmNegative2VsTwoKgMNegative2_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2000,
        listOf(UNIT_G_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitConversion_realPoint2GcmNegative2VsTwoKgMNegative2_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForReal(
        number = 0.2,
        units = listOf(UNIT_G_TO_POWER_1, UNIT_CM_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitConversion_fractionTwoOverThirtyGcmNegative2VsTwoThirdsKgMNegative2_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2_OVER_30,
        listOf(UNIT_G_TO_POWER_1, UNIT_CM_TO_POWER_NEGATIVE_2)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2_OVER_3,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_NEGATIVE_2)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitConversion_twoRupeesVsTwoHundredPaise_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_RUPEES_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_200,
          listOf(UNIT_PAISE_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitConversion_twoDollarsVsTwoHundredCents_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_DOLLARS_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_200,
          listOf(UNIT_CENTS_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitReduction_twentyMSquareSecondPerSecondVsTwentyMPerSecond_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_20,
        listOf(UNIT_M_TO_POWER_1, UNIT_S_TO_POWER_2, UNIT_S_TO_POWER_NEGATIVE_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_20,
          listOf(UNIT_M_TO_POWER_1, UNIT_S_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitCombination_tenNewtonMeterVsTenJoule_doesNotMatch() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_10,
        listOf(UNIT_N_TO_POWER_1, UNIT_M_TO_POWER_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_10,
          listOf(UNIT_J_TO_POWER_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUnitReordering_twoMKgPerSecondVsTwoKgMPerSecond_matches() {
    val inputs = mapOf(
      "f" to InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
        FRACTION_VALUE_TEST_2,
        listOf(UNIT_M_TO_POWER_1, UNIT_KG_TO_POWER_1, UNIT_S_TO_POWER_NEGATIVE_1)
      )
    )

    val matches =
      unitsIsEqualsRuleClassifier.matches(
        answer = InteractionObjectTestBuilder.createNumberWithUnitsForFraction(
          FRACTION_VALUE_TEST_2,
          listOf(UNIT_KG_TO_POWER_1, UNIT_M_TO_POWER_1, UNIT_S_TO_POWER_NEGATIVE_1)
        ),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testWholeNumberInputValue_withRealAnswerNumWithUnits_throwsException() {
    val inputs = mapOf("f" to WHOLE_NUMBER_VALUE_9)

    val exception = assertThrows<IllegalStateException>() {

      unitsIsEqualsRuleClassifier.matches(
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

  private fun setUpTestApplicationComponent() {
    DaggerNumberWithUnitsIsEqualToRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: NumberWithUnitsIsEqualToRuleClassifierProviderTest)
  }
}
