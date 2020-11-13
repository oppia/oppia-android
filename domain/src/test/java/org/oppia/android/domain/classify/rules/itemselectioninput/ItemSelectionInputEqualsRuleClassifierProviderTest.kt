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
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.StringList
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
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

  private val TEST_HTML_STRING_SET_LOWERCASE =
    createSetOfHtmlString(
      InteractionObjectTestBuilder.createHtmlStringList("item ab")
    )

  private val TEST_HTML_STRING_SET_UPPERCASE =
    createSetOfHtmlString(
      InteractionObjectTestBuilder.createHtmlStringList("item AB")
    )

  private val TEST_HTML_STRING_SET_MIXEDLOWERCASE =
    createSetOfHtmlString(
      InteractionObjectTestBuilder.createHtmlStringList("item aA ")
    )
  private val TEST_HTML_STRING_SET_MIXEDUPPERCASE =
    createSetOfHtmlString(
      InteractionObjectTestBuilder.createHtmlStringList("item bB")
    )

  private val TEST_NON_NEGATIVE_VALUE_3 =
    createSetOfHtmlString(
      InteractionObjectTestBuilder.createHtmlStringList("item AB invalid")
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
  fun testLowercaseStringAnswer_samevalue_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withlowercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withuppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withlowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withuppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_mixedlowercaseAnswer_withmixeduppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_MIXEDLOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_MIXEDUPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withuppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withlowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testlowercaseAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to TEST_HTML_STRING_SET_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  private fun createSetOfHtmlString(value: StringList): InteractionObject {
    return InteractionObject.newBuilder().setSetOfHtmlString(value).build()
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
      // Unexpected exception; throw it.
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
