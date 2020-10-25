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

  private val STRING_VALUE_TESTSTRING_LOWERCASE = createString("test string")
  private val STRING_VALUE_TESTSTRING_LOWERCASE_EXTRA_SPACES = createString("test  string")
  private val STRING_VALUE_TEST_LOWERCASE = createString("test")
  private val STRING_VALUE_STRING_LOWERCASE = createString("string")
  private val STRING_VALUE_TESTSTRING_UPPERCASE = createString("TEST STRING")
  private val STRING_VALUE_TESTSTRING_UPPERCASE_NO_SPACES = createString("TESTSTRING")
  private val STRING_VALUE_TEST_UPPERCASE = createString("TEST")
  private val EMPTY_STRING = createString("")

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_differentStrings_verifyAnsStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_differentString_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_STRING_LOWERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_inputStartsWithAns_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_LOWERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_uppercaseStringInput_sameCaseInsensitive_verifyAnsDoesNotStartWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_UPPERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_extraSpaces_verifyAnsStartWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_LOWERCASE_EXTRA_SPACES)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_uppercaseStringInput_differentStrings_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_emptyStringInput_differentStrings_verifyAnsStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUppercaseStringAns_uppercaseStringInput_inputStartWithAns_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_UPPERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseStringAns_uppercaseStringInput_noSpaces_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_UPPERCASE_NO_SPACES)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseStringAns_emptyStringInput_differentStrings_verifyAnsStartWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TESTSTRING_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAns_lowercaseStringInput_differentStrings_verifyAnsDoesNotStartsWith() {
    val inputs = mapOf("x" to STRING_VALUE_TESTSTRING_LOWERCASE)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAns_emptyStringInput_exactSameStrings_verifyAnsStartsWith() {
    val inputs = mapOf("x" to EMPTY_STRING)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = EMPTY_STRING,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAns_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TESTSTRING_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = STRING_VALUE_TESTSTRING_LOWERCASE,
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
        answer = STRING_VALUE_TESTSTRING_LOWERCASE,
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
