package org.oppia.android.domain.classify.rules.textinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
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

/** Tests for [TextInputCaseSensitiveEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputCaseSensitiveEqualsRuleClassifierProviderTest {
  private val STRING_VALUE_TEST_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_LOWERCASE = createString(value = "test")
  private val STRING_VALUE = createString(value = "string")

  @Inject
  internal lateinit var textInputCaseSensitiveEqualsRuleClassifierProvider:
    TextInputCaseSensitiveEqualsRuleClassifierProvider

  private val inputCaseSensitiveEqualsRuleClassifier by lazy {
    textInputCaseSensitiveEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testUppercaseStringAnswer_uppercaseStringInput_sameExactString_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE)

    val matches =
      inputCaseSensitiveEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs) // ktlint-disable max-line-length

    Truth.assertThat(matches).isTrue()
  }

  @Test
  fun testUppercaseStringAnswer_lowercaseStringInput_sameExactString_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val matches =
      inputCaseSensitiveEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs) // ktlint-disable max-line-length

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseStringAnswer_stringInput_differentString_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE)

    val matches =
      inputCaseSensitiveEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs) // ktlint-disable max-line-length

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputCaseSensitiveEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_LOWERCASE, inputs = inputs) // ktlint-disable max-line-length
    }

    Truth.assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputCaseSensitiveEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
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

    fun inject(test: TextInputCaseSensitiveEqualsRuleClassifierProviderTest)
  }
}
