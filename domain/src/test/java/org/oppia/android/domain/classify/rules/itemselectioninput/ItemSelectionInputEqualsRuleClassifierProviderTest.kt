package org.oppia.android.domain.classify.rules.itemselectioninput

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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createSetOfHtmlString
import org.oppia.android.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [ItemsSelectionInputIsEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputEqualsRuleClassifierProviderTest {

  private val TEST_HTML_STRING_SET_LOWERCASE = createSetOfHtmlString(
    InteractionObjectTestBuilder.createHtmlStringList("item ab")
  )

  private val TEST_HTML_STRING_SET_UPPERCASE = createSetOfHtmlString(
    InteractionObjectTestBuilder.createHtmlStringList("item AB")
  )

  private val TEST_HTML_STRING_SET_MIXED_LOWERCASE = createSetOfHtmlString(
    InteractionObjectTestBuilder.createHtmlStringList("item Aa ")
  )
  private val TEST_HTML_STRING_SET_MIXED_UPPERCASE = createSetOfHtmlString(
    InteractionObjectTestBuilder.createHtmlStringList("item Bb")
  )

  private val NON_NEGATIVE_VALUE_3 = createSetOfHtmlString(
    InteractionObjectTestBuilder.createHtmlStringList("3")
  )

  @Inject
  internal lateinit var itemSelectionInputEqualsRuleClassifierProvider:
    ItemSelectionInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifierProvider: RuleClassifier by lazy {
    itemSelectionInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLowercaseStringAnswer_sameValue_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withLowercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withUppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withLowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withUppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE, inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_mixedLowercaseAnswer_withMixedUppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_MIXED_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_MIXED_UPPERCASE, inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withUppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE, inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withLowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to TEST_HTML_STRING_SET_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE, inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputEqualsRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }

      throw t
    }
  }

  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ItemSelectionInputEqualsRuleClassifierProviderTest)
  }
}
