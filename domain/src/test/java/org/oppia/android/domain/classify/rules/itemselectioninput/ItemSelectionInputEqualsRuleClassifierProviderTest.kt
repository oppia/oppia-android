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
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createSetOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputEqualsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputEqualsRuleClassifierProviderTest {

  private val TEST_HTML_STRING_SET_LOWERCASE = createSetOfTranslatableHtmlContentIds("item ab")
  private val TEST_HTML_STRING_SET_UPPERCASE = createSetOfTranslatableHtmlContentIds("item AB")
  private val TEST_HTML_STRING_SET_MIXED_LOWERCASE =
    createSetOfTranslatableHtmlContentIds("item Aa ")
  private val TEST_HTML_STRING_SET_MIXED_UPPERCASE =
    createSetOfTranslatableHtmlContentIds("item Bb")
  private val NON_NEGATIVE_VALUE_3 = createSetOfTranslatableHtmlContentIds("3AB")

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
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withLowercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAnswer_withUppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withLowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_uppercaseAnswer_withUppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_mixedLowercaseAnswer_withMixedUppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to TEST_HTML_STRING_SET_MIXED_LOWERCASE)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_MIXED_UPPERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withUppercaseAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_UPPERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withLowercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to TEST_HTML_STRING_SET_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifierProvider.matches(
        answer = TEST_HTML_STRING_SET_LOWERCASE,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputEqualsRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
