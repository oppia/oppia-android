package org.oppia.domain.classify.rules.ratioinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.RatioExpression
import org.oppia.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [RatioInputHasNumberOfTermsEqualToClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioInputHasNumberOfTermsEqualToClassifierProviderTest {
  private val NON_NEGATIVE_VALUE_3 = createNonNegativeInt(value = 3)
  private val NON_NEGATIVE_VALUE_4 = createNonNegativeInt(value = 4)
  private val RATIO_WITH_THREE_TERMS = listOf(1, 2, 3)

  @Inject
  internal lateinit var ratioInputHasNumberOfTermsEqualToClassifierProvider:
    RatioInputHasNumberOfTermsEqualToClassifierProvider

  private val hasNumberOfTermsEqualToClassifierProvider: RuleClassifier by lazy {
    ratioInputHasNumberOfTermsEqualToClassifierProvider.createRuleClassifier()
  }

  @Test
  fun testAnswer_withThreeTerms_expected3_matchesCorrectly() {
    val inputs = mapOf("y" to NON_NEGATIVE_VALUE_3)

    val matches =
      hasNumberOfTermsEqualToClassifierProvider.matches(
        answer = createRatio(RATIO_WITH_THREE_TERMS),
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_withThreeTerms_expectedFour_doesNotMatch() {
    val inputs = mapOf("y" to NON_NEGATIVE_VALUE_4)

    val matches =
      hasNumberOfTermsEqualToClassifierProvider.matches(
        answer = createRatio(RATIO_WITH_THREE_TERMS),
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("y" to createRatio(RATIO_WITH_THREE_TERMS))

    val exception = assertThrows(IllegalStateException::class) {
      hasNumberOfTermsEqualToClassifierProvider.matches(
        answer = createRatio(RATIO_WITH_THREE_TERMS),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type NON_NEGATIVE_INT not RATIO_EXPRESSION"
      )
  }

  @Test
  fun testAnswer_testRatio_missingInputY_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_4)

    val exception = assertThrows(IllegalStateException::class) {
      hasNumberOfTermsEqualToClassifierProvider.matches(
        answer = createRatio(RATIO_WITH_THREE_TERMS),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'y' but had: [x]")
  }

  private fun createRatio(value: List<Int>): InteractionObject {
    return InteractionObject.newBuilder().setRatioExpression(
      RatioExpression.newBuilder().addAllRatioComponent(value)
    ).build()
  }

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerRatioInputHasNumberOfTermsEqualToClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: RatioInputHasNumberOfTermsEqualToClassifierProviderTest)
  }
}
