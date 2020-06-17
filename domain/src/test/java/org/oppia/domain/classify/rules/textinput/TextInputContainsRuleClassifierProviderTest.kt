package org.oppia.domain.classify.rules.textinput

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
import org.robolectric.annotation.Config
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [TextInputContainsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TextInputContainsRuleClassifierProviderTest {

  @Inject
  internal lateinit var textInputContainsRuleClassifierProvider:
    TextInputContainsRuleClassifierProvider

  private val inputContainsRuleClassifier by lazy {
    textInputContainsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameString_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = "this is a test"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAnswer_emptyStringInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = ""))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = ""), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_emptyStringInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = ""))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtBeginning_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = "this is"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputInMiddle_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = "is a"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtEnd_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = "a test"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringExtraSpacesInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to createString(value = " this   is  a  test "))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_inputNotInAnswer_valuesDoNotMatch() {
    val inputs = mapOf("x" to createString(value = "an answer"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAnswer_nonEmptyStringInput_answerDoesNotContainInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to createString(value = "this is a test"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = ""), inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerPartiallyContainsInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to createString(value = "this is a test i will break"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_answerPartiallyContainsInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to createString(value = "thisisatest"))

    val matches = inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to createString(value = "this is a test"))

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to createNonNegativeInt(value = 1))

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(answer = createString(value = "this is a test"), inputs = inputs)
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
    DaggerTextInputContainsRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: TextInputContainsRuleClassifierProviderTest)
  }
}
