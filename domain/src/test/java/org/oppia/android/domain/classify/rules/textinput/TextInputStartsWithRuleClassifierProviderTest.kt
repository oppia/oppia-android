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

  @Inject
  internal lateinit var textInputStartsWithRuleClassifierProvider:
    TextInputStartsWithRuleClassifierProvider

  private val inputStartsWithRuleClassifier by lazy {
    textInputStartsWithRuleClassifierProvider.createRuleClassifier()
  }

  private val LOWERCASE_1 = createString("test string")
  private val LOWERCASE_2 = createString("test")
  private val LOWERCASE_3 = createString("string")
  private val UPPERCASE_1 = createString("TEST STRING")
  private val UPPERCASE_2 = createString("TEST")
  private val EMPTY_STRING = createString("")

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLowercaseAns_testStringLowercaseInput_outputAsTestString_verifyAnsStartsWith() {
    val inputs = mapOf("x" to LOWERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseAns_testLowercaseInput_outputAsTestString_verifyAnsStartsWith() {
    val inputs = mapOf("x" to LOWERCASE_2)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseAns_stringLowercaseInput_outputAsTestString_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to LOWERCASE_3)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAns_testStringLowercaseInput_outputAsTest_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to LOWERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAns_testStringUppercaseInput_outputAsTestString_verifyAnsDoesNotStartWith() {
    val inputs = mapOf("x" to UPPERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAns_testUppercaseInput_outputAsTestString_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to UPPERCASE_2)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAns_emptyStringInput_outputAsTestString_verifyAnsStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = LOWERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUppercaseAns_testStringUppercaseInput_outputAsTestString_verifyAnsStartsWith() {
    val inputs = mapOf("x" to UPPERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = UPPERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUppercaseAns_testStringUppercaseInput_outputAsTest_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to UPPERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = UPPERCASE_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseAns_emptyStringInput_outputAsTestString_verifyAnsStartWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = UPPERCASE_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAns_testStringLowercaseInput_outputAsEmptyString_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to LOWERCASE_1)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAns_emptyStringInput_verifyAnsStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAns_missingInput_throwsException() {
    val inputs = mapOf("y" to LOWERCASE_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = LOWERCASE_1,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAns_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to createNonNegativeInt(value = 1))

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = LOWERCASE_1,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING")
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
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
