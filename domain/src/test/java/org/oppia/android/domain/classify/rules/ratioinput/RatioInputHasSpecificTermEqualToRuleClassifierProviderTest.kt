package org.oppia.android.domain.classify.rules.ratioinput

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
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [RatioInputHasSpecificTermEqualToRuleClassifierProvider]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioInputHasSpecificTermEqualToRuleClassifierProviderTest {
  @Inject
  internal lateinit var ratioInputHasSpecificTermEqualToRuleClassifierProvider:
    RatioInputHasSpecificTermEqualToRuleClassifierProvider

  private val hasSpecificTermEqualToClassifierProvider: RuleClassifier by lazy {
    ratioInputHasSpecificTermEqualToRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_oneTerm_expectFirstIndex_matchingValue_matchesCorrectly() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1, "y" to NON_NEGATIVE_VALUE_TEST_3)

    // Note that the test for a single-value ratio is just for robustness since the classifier
    // technically allows this even though the data definition specifies that it should never
    // happen.
    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_oneTerm_expectFirstIndex_unmatchingValue_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1, "y" to NON_NEGATIVE_VALUE_TEST_2)

    // Note that the test for a single-value ratio is just for robustness since the classifier
    // technically allows this even though the data definition specifies that it should never
    // happen.
    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // The ratio has value 3, but the value 2 was expected.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_oneTerm_expectSecondIndex_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_2, "y" to NON_NEGATIVE_VALUE_TEST_2)

    // Note that the test for a single-value ratio is just for robustness since the classifier
    // technically allows this even though the data definition specifies that it should never
    // happen.
    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // A value was expected at index 2, but the ratio doesn't have that.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_twoTerms_expectFirstIndex_matchingValue_matchesCorrectly() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1, "y" to NON_NEGATIVE_VALUE_TEST_3)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3_2,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_twoTerms_expectFirstIndex_unmatchingValue_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1, "y" to NON_NEGATIVE_VALUE_TEST_2)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3_2,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // The ratio has value 3 at index 1, but the value 2 was expected.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_twoTerms_expectSecondIndex_matchingValue_matchesCorrectly() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_2, "y" to NON_NEGATIVE_VALUE_TEST_2)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3_2,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_twoTerms_expectSecondIndex_unmatchingValue_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_2, "y" to NON_NEGATIVE_VALUE_TEST_3)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3_2,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // The ratio has value 2 at index 2, but the value 3 was expected.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_twoTerms_expectThirdIndex_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_3, "y" to NON_NEGATIVE_VALUE_TEST_3)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_3_2,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // A value was expected at index 3, but the ratio doesn't have that.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_threeTerms_expectZeroIndex_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0, "y" to NON_NEGATIVE_VALUE_TEST_1)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // A value was expected at index 0, but the ratio doesn't have that.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_threeTerms_expectFourthIndex_doesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_4, "y" to NON_NEGATIVE_VALUE_TEST_3)

    val matches =
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    // A value was expected at index 4, but the ratio doesn't have that.
    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_threeTerms_indexInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE, "y" to NON_NEGATIVE_VALUE_TEST_3)

    val exception = assertThrows(IllegalStateException::class) {
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NON_NEGATIVE_INT not NORMALIZED_STRING")
  }

  @Test
  fun testAnswer_threeTerms_valueInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_4, "y" to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NON_NEGATIVE_INT not NORMALIZED_STRING")
  }

  @Test
  fun testAnswer_threeTerms_missingIndexInput_throwsException() {
    val inputs = mapOf("y" to NON_NEGATIVE_VALUE_TEST_3)

    val exception = assertThrows(IllegalStateException::class) {
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  @Test
  fun testAnswer_threeTerms_missingValueInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_4)

    val exception = assertThrows(IllegalStateException::class) {
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'y' but had: [x]")
  }

  @Test
  fun testAnswer_threeTerms_missingBothInputs_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      hasSpecificTermEqualToClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = mapOf(),
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: []")
  }

  private fun setUpTestApplicationComponent() {
    DaggerRatioInputHasSpecificTermEqualToRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: RatioInputHasSpecificTermEqualToRuleClassifierProviderTest)
  }

  private companion object {
    private val NON_NEGATIVE_VALUE_TEST_0 = InteractionObjectTestBuilder.createNonNegativeInt(0)
    private val NON_NEGATIVE_VALUE_TEST_1 = InteractionObjectTestBuilder.createNonNegativeInt(1)
    private val NON_NEGATIVE_VALUE_TEST_2 = InteractionObjectTestBuilder.createNonNegativeInt(2)
    private val NON_NEGATIVE_VALUE_TEST_3 = InteractionObjectTestBuilder.createNonNegativeInt(3)
    private val NON_NEGATIVE_VALUE_TEST_4 = InteractionObjectTestBuilder.createNonNegativeInt(4)
    private val STRING_VALUE = InteractionObjectTestBuilder.createString("test str")
    private val RATIO_VALUE_TEST_3 = InteractionObjectTestBuilder.createRatio(listOf(3))
    private val RATIO_VALUE_TEST_3_2 = InteractionObjectTestBuilder.createRatio(listOf(3, 2))
    private val RATIO_VALUE_TEST_1_2_3 = InteractionObjectTestBuilder.createRatio(listOf(1, 2, 3))
  }
}
