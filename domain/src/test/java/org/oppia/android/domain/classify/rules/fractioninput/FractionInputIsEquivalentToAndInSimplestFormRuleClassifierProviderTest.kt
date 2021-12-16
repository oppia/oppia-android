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
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProviderTest {

  private val WHOLE_NUMBER_VALUE_TEST_5 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = false,
      value = 5
    )
  private val WHOLE_NUMBER_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = false,
      value = 2
    )
  private val NEGATIVE_WHOLE_NUMBER_VALUE_TEST_2 =
    InteractionObjectTestBuilder.createWholeNumber(
      isNegative = true,
      value = 2
    )
  private val FRACTION_VALUE_TEST_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 1,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_4_OVER_8 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 4,
      denominator = 8
    )
  private val NEGATIVE_FRACTION_VALUE_TEST_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 16,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_16_AND_2_OVER_4 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 16,
      numerator = 2,
      denominator = 4
    )
  private val MIXED_NUMBER_VALUE_TEST_7_AND_1_OVER_7 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 7,
      numerator = 1,
      denominator = 7
    )
  private val NEGATIVE_MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 16,
      numerator = 1,
      denominator = 2
    )
  private val STRING_VALUE_TEST =
    InteractionObjectTestBuilder.createString(
      value = "test string"
    )

  @Inject
  internal lateinit var fractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider:
    FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider

  private val inputIsEquivalentToAndInSimplestFormRuleClassifier by lazy {
    fractionInputIsEquivalentToAndInSimplestFormRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testEquivalentAndSimplest_wholeNumber5Answer_wholeNumber5Input_matches() {
    val inputs = mapOf("f" to WHOLE_NUMBER_VALUE_TEST_5)
    val answer = WHOLE_NUMBER_VALUE_TEST_5

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquivalentAndSimplest_wholeNumber5Answer_wholeNumber2Input_doesNotMatch() {
    val inputs = mapOf("f" to WHOLE_NUMBER_VALUE_TEST_5)
    val answer = WHOLE_NUMBER_VALUE_TEST_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_wholeNumber2Answer_negativeWholeNumber2Input_doesNotMatch() {
    val inputs = mapOf("f" to NEGATIVE_WHOLE_NUMBER_VALUE_TEST_2)
    val answer = WHOLE_NUMBER_VALUE_TEST_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_fraction1Over2Answer_fraction1Over2Input_matches() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)
    val answer = FRACTION_VALUE_TEST_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquivalentAndSimplest_fraction4Over8Answer_fraction1Over2Input_doesNotMatch() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)
    val answer = FRACTION_VALUE_TEST_4_OVER_8

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_fraction1Over2Answer_fraction4Over8Input_matches() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_4_OVER_8)
    val answer = FRACTION_VALUE_TEST_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // Even if creator does not input simplest form, learner's answer must still be in simplest form
    assertThat(matches).isTrue()
  }

  @Test
  fun testEquivalentAndSimplest_fraction4Over8Answer_fraction4Over8Input_doesNotMatch() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_4_OVER_8)
    val answer = FRACTION_VALUE_TEST_4_OVER_8

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_fraction1Over2Answer_negativeFraction1Over2Input_doesNotMatch() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_VALUE_TEST_1_OVER_2)
    val answer = FRACTION_VALUE_TEST_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_mixedNum16And1Over2Answer_mixedNum6And1Over2Input_matches() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2)
    val answer = MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquivalentAndSimplest_mixedNum16And1Over2Answer_mixedNum7And1Over7Input_doesNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_7_AND_1_OVER_7)
    val answer = MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_mixedNum16And2Over4Answer_mixedNum6And1Over2Input_doesNotMatch() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2)
    val answer = MIXED_NUMBER_VALUE_TEST_16_AND_2_OVER_4

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_mixedNum16And1Over2Ans_negMixedNum16And1Over2Input_doesNotMatch() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2)
    val answer = MIXED_NUMBER_VALUE_TEST_16_AND_1_OVER_2

    val matches =
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquivalentAndSimplest_missingInputF_throwsException() {
    val inputs = mapOf("y" to FRACTION_VALUE_TEST_1_OVER_2)
    val answer = FRACTION_VALUE_TEST_1_OVER_2

    val exception = assertThrows(IllegalStateException::class) {
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
  }

  @Test
  fun testEquivalentAndSimplest_typeStringAnswer_fraction1Over2Input_throwsException() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)
    val answer = STRING_VALUE_TEST

    val exception = assertThrows(IllegalStateException::class) {
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected answer to be of type FRACTION")
  }

  @Test
  fun testEquivalentAndSimplest_fraction1Over2Answer_typeStringInput_throwsException() {
    val inputs = mapOf("f" to STRING_VALUE_TEST)
    val answer = FRACTION_VALUE_TEST_1_OVER_2

    val exception = assertThrows(IllegalStateException::class) {
      inputIsEquivalentToAndInSimplestFormRuleClassifier.matches(
        answer = answer,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type FRACTION")
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputIsEquivalentToAndInSimplestFormRuleClassifierProviderTest_TestApplicationComponent // ktlint-disable max-line-length
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(test: FractionInputIsEquivalentToAndInSimplestFormRuleClassifierProviderTest)
  }
}
