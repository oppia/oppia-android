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

/** Tests for [RatioInputHasNumberOfTermsEqualsToClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class RatioInputHasNumberOfTermsEqualsToClassifierProviderTest {
  private val NON_NEGATIVE_VALUE_3 = createNonNegativeInt(value = 3)
  private val ITEM_RATIO_3TERMS = listOf(1, 2, 3)
  private val ITEM_RATIO_4TERMS = listOf(2, 4, 6, 8)

  @Inject
  internal lateinit var ratioInputHasNumberOfTermsEqualsToClassifierProvider:
    RatioInputHasNumberOfTermsEqualsToClassifierProvider

  private val hasNumberOfTermsEqualsToClassifierProvider: RuleClassifier by lazy {
    ratioInputHasNumberOfTermsEqualsToClassifierProvider.createRuleClassifier()
  }

  @Test
  fun testAnswer_testRatio_ratio3Terms_bothValuesMatch() {
    val inputs = mapOf("y" to createRatio(ITEM_RATIO_3TERMS))

    val matches =
      hasNumberOfTermsEqualsToClassifierProvider.matches(
        answer = NON_NEGATIVE_VALUE_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testRatio_ratio4Terms_bothValuesDoNoMatch() {
    val inputs = mapOf("y" to createRatio(ITEM_RATIO_4TERMS))

    val matches =
      hasNumberOfTermsEqualsToClassifierProvider.matches(
        answer = NON_NEGATIVE_VALUE_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("y" to NON_NEGATIVE_VALUE_3)

    val exception = assertThrows(IllegalStateException::class) {
      hasNumberOfTermsEqualsToClassifierProvider.matches(
        answer = NON_NEGATIVE_VALUE_3,
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
  fun testAnswer_testRatio_missingInputY_throwsException() {
    val inputs = mapOf("x" to createRatio(ITEM_RATIO_3TERMS))

    val exception = assertThrows(IllegalStateException::class) {
      hasNumberOfTermsEqualsToClassifierProvider.matches(
        answer = NON_NEGATIVE_VALUE_3,
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
    DaggerRatioInputHasNumberOfTermsEqualsToClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: RatioInputHasNumberOfTermsEqualsToClassifierProviderTest)
  }
}
