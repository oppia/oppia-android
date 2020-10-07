package org.oppia.android.domain.classify.rules.textinput

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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [TextInputStartsWithRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputStartsWithRuleClassifierProviderTest {

  private val TEST_STRING_1 = createString("This is test")
  private val TEST_STRING_2 = createString("This is")
  private val TEST_STRING_3 = createString("is test string")
  private val EMPTY_STRING = createString("")

  @Inject
  internal lateinit var textInputStartsWithRuleClassifierProvider:
    TextInputStartsWithRuleClassifierProvider

  private val inputStartsWithRuleClassifier by lazy {
    textInputStartsWithRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameExactValue_verifyAnswerStartsWith() {
    val inputs = mapOf("x" to TEST_STRING_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = TEST_STRING_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_differentValue_verifyAnswerStartsWith() {
    val inputs = mapOf("x" to TEST_STRING_2)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = TEST_STRING_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_differentValue_verifyAnswerNotStartsWith() {
    val inputs = mapOf("x" to TEST_STRING_3)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = TEST_STRING_1,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerSizeSmaller_verifyAnswerNotStartsWith() {
    val inputs = mapOf("x" to TEST_STRING_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = TEST_STRING_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_emptyInput_differentValue_verifyAnswerStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = TEST_STRING_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAnswer_stringInput_answerSizeSmaller_verifyAnswerNotStartsWith() {
    val inputs = mapOf("x" to TEST_STRING_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAnswer_emptyStringInput_sameExactValue_verifyAnswerStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to createString(value = "this is a test"))

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = createString(value = "this is a test"),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to createNonNegativeInt(value = 1))

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = createString(value = "this is a test"),
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING")
  }

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputStartsWithRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    fun inject(test: TextInputStartsWithRuleClassifierProviderTest)
  }
}
