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
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [TextInputEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TextInputEqualsRuleClassifierProviderTest {
  private val STRING_VALUE_TEST_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_LOWERCASE = createString(value = "test")
  private val STRING_VALUE = createString(value = "string")
  private val STRING_VALUE_TEST_EXTRA_SPACES = createString(value = "test  a  lot  ")
  private val STRING_VALUE_TEST_SINGLE_SPACES = createString(value = "test a lot")
  private val STRING_VALUE_TEST_NO_SPACES = createString(value = "testalot")
  private val NON_NEGATIVE_VALUE = createNonNegativeInt(value = 1)

  @Inject
  internal lateinit var textInputEqualsRuleClassifierProvider:
    TextInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    textInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameExactString_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testCapitalStringAnswer_lowercaseStringInput_sameCaseInsensitiveString_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAnswer_capitalStringInput_sameCaseInsensitiveString_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_sameStringDifferentSpaces_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACES)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_SINGLE_SPACES, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_differentStrings_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val matches = inputEqualsRuleClassifier.matches(answer = STRING_VALUE, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_differentStrings_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACES)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_SINGLE_SPACES, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_LOWERCASE, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)
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
    DaggerTextInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: TextInputEqualsRuleClassifierProviderTest)
  }
}
