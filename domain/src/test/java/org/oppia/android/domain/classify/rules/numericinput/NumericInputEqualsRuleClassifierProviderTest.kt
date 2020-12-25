package org.oppia.android.domain.classify.rules.numericinput

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
import org.oppia.android.domain.util.FLOAT_EQUALITY_INTERVAL
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NumericInputEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericInputEqualsRuleClassifierProviderTest {

  private val STRING_VALUE_X =
    InteractionObjectTestBuilder.createString(value = "x")

  private val POSITIVE_REAL_VALUE_1_5 =
    InteractionObjectTestBuilder.createReal(value = 1.5)

  private val POSITIVE_REAL_VALUE_3_5 =
    InteractionObjectTestBuilder.createReal(value = 3.5)

  private val NEGATIVE_REAL_VALUE_1_5 =
    InteractionObjectTestBuilder.createReal(value = -1.5)

  private val NEGATIVE_REAL_VALUE_3_5 =
    InteractionObjectTestBuilder.createReal(value = -3.5)

  private val STRING_VALUE =
    InteractionObjectTestBuilder.createString(value = "test")

  @Inject
  internal lateinit var numericInputEqualsRuleClassifierProvider:
    NumericInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    numericInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_sameExactValue_bothValuesMatch() {
    val inputs = mapOf(STRING_VALUE_X to POSITIVE_REAL_VALUE_1_5)

    val matches =
      inputEqualsRuleClassifier.matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_sameExactValue_bothValuesMatch() {
    val inputs = mapOf(STRING_VALUE_X to NEGATIVE_REAL_VALUE_1_5)

    val matches =
      inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_valuesInRange_bothValuesMatch() {
    val inputs = mapOf(
      STRING_VALUE_X to InteractionObjectTestBuilder.createReal(
        value = 5 * FLOAT_EQUALITY_INTERVAL
      )
    )

    val matches = inputEqualsRuleClassifier.matches(
      answer = InteractionObjectTestBuilder.createReal(
        value = 5 * FLOAT_EQUALITY_INTERVAL +
          FLOAT_EQUALITY_INTERVAL / 10
      ),
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf(STRING_VALUE_X to POSITIVE_REAL_VALUE_1_5)

    val matches =
      inputEqualsRuleClassifier.matches(answer = POSITIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf(STRING_VALUE_X to NEGATIVE_REAL_VALUE_1_5)

    val matches =
      inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_positiveRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf(STRING_VALUE_X to POSITIVE_REAL_VALUE_3_5)

    val matches =
      inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_valueAtRange_valuesDoNotMatch() {
    val inputs = mapOf(
      STRING_VALUE_X to InteractionObjectTestBuilder.createReal(
        value = 5 * FLOAT_EQUALITY_INTERVAL
      )
    )

    val matches = inputEqualsRuleClassifier.matches(
      answer = InteractionObjectTestBuilder.createReal(value = 6 * FLOAT_EQUALITY_INTERVAL),
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testRealAnswer_missingInput_throwsException() {
    val inputs = mapOf(STRING_VALUE_Y to POSITIVE_REAL_VALUE_1_5)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testRealAnswer_stringInput_throwsException() {
    val inputs = mapOf(STRING_VALUE_X to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumericInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    fun inject(test: NumericInputEqualsRuleClassifierProviderTest)
  }
}
