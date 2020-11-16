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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.oppia.android.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [RatioInputIsEquivalentRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioInputIsEquivalentRuleClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_TEST_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(
      value = 0
    )
  private val RATIO_VALUE_TEST_1_2_3 =
    InteractionObjectTestBuilder.createRatio(
      listOf(1, 2, 3)
    )
  private val RATIO_VALUE_TEST_2_4_6 =
    InteractionObjectTestBuilder.createRatio(
      listOf(2, 4, 6)
    )
  private val RATIO_VALUE_TEST_2_4_6_8 =
    InteractionObjectTestBuilder.createRatio(
      listOf(2, 4, 6, 8)
    )
  private val RATIO_VALUE_TEST_2_3_5 =
    InteractionObjectTestBuilder.createRatio(
      listOf(2, 3, 5)
    )

  @Inject
  internal lateinit var ratioInputIsEquivalentRuleClassifierProvider:
    RatioInputIsEquivalentRuleClassifierProvider

  private val isEquivalentClassifierProvider: RuleClassifier by lazy {
    ratioInputIsEquivalentRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_testRatio_ratioNonReduced_bothValuesMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_2_4_6,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testRatio_ratioNonReduced_ratioReduced_bothValuesMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testRatio_ratioReduced_bothValuesMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_1_2_3)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testRatio_ratio1_ratio3_differentLengths_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6_8)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testRatio_ratio1_ratio4_differentRatios_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_3_5)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testRatio_ratio2NonReduced_ratio1Reduced_bothValuesMatch() {
    val inputs = mapOf("x" to RATIO_VALUE_TEST_2_4_6)

    val matches =
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type RATIO_EXPRESSION not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testAnswer_testRatio_missingInputX_throwsException() {
    val inputs = mapOf("y" to RATIO_VALUE_TEST_1_2_3)

    val exception = assertThrows(IllegalStateException::class) {
      isEquivalentClassifierProvider.matches(
        answer = RATIO_VALUE_TEST_1_2_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerRatioInputIsEquivalentRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move to a common test library.
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

    fun inject(test: RatioInputIsEquivalentRuleClassifierProviderTest)
  }
}
