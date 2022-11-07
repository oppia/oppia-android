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

/** Tests for [FractionInputHasIntegerPartEqualToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputHasIntegerPartEqualToRuleClassifierProviderTest {

  // TODO: Add tests for negative cases?
  private val FRACTION_VALUE_TEST_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 1,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_5_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 5,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_3_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 3,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 1,
      denominator = 3
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_5_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 5,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_3_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 3,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_123_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 123,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_1_2_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 1,
      numerator = 2,
      denominator = 3
    )
  private val MIXED_NUMBER_VALUE_TEST_0_2_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 0,
      numerator = 2,
      denominator = 3
    )
  private val MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 123,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_NEGATIVE_1_2_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 1,
      numerator = 2,
      denominator = 3
    )
  private val MIXED_NUMBER_VALUE_TEST_NEGATIVE_0_2_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 0,
      numerator = 2,
      denominator = 3
    )
  private val STRING_VALUE_TEST_LOWERCASE =
    InteractionObjectTestBuilder.createString(
      value = "test"
    )
  private val WHOLE_NUMBER_VALUE_TEST_0 = InteractionObjectTestBuilder.createSignedInt(value = 0)
  private val WHOLE_NUMBER_VALUE_TEST_1 = InteractionObjectTestBuilder.createSignedInt(value = 1)
  private val WHOLE_NUMBER_VALUE_TEST_2 = InteractionObjectTestBuilder.createSignedInt(value = 2)
  private val WHOLE_NUMBER_VALUE_TEST_3 = InteractionObjectTestBuilder.createSignedInt(value = 3)
  private val WHOLE_NUMBER_VALUE_TEST_123 =
    InteractionObjectTestBuilder.createSignedInt(value = 123)

  @Inject
  internal lateinit var fractionInputHasIntegerPartEqualToRuleClassifier:
    FractionInputHasIntegerPartEqualToRuleClassifierProvider

  private val inputHasIntegerPartEqualToRuleClassifier by lazy {
    fractionInputHasIntegerPartEqualToRuleClassifier.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer1Over2_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer5Over2_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_5_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer5Over2_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_5_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer3Over2_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_3_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer3Over2_input3_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_3)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_3_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over2_input1_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_1)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer123_1Over2_input123_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_123)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer0_2Over3_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_0_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1_2Over3_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_1_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1_2Over3_input3_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_3)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_1_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative123_1Over2_input123_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_123)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative0_2Over3_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_0_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative1Over2_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative5Over2_input0_hasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_5_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative5Over2_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_5_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative3Over2_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_3_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative3Over2_input3_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_3)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_3_OVER_2,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  fun testAnswerNegative1_2Over3_input2_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_1_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1_2Over3_input3_hasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_VALUE_TEST_3)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_1_2_OVER_3,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over2_inputMissing_throwsException() {
    val inputs = mapOf("y" to WHOLE_NUMBER_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      inputHasIntegerPartEqualToRuleClassifier
        .matches(
          answer = FRACTION_VALUE_TEST_1_OVER_2,
          inputs = inputs,
          classificationContext = ClassificationContext()
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  @Test
  fun testAnswer1Over2_inputString_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputHasIntegerPartEqualToRuleClassifier
        .matches(
          answer = FRACTION_VALUE_TEST_1_OVER_2,
          inputs = inputs,
          classificationContext = ClassificationContext()
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type SIGNED_INT not NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputHasIntegerPartEqualToRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(test: FractionInputHasIntegerPartEqualToRuleClassifierProviderTest)
  }
}
