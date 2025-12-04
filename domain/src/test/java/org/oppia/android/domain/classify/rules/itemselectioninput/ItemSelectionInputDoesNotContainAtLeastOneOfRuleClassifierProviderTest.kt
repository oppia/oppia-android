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
import org.oppia.android.app.player.state.testing.InteractionObjectTestBuilder.createInt
import org.oppia.android.app.player.state.testing.InteractionObjectTestBuilder.createSetOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider]. */
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProviderTest {
  @Inject
  internal lateinit var itemSelectionInputDesNotContainAtLeastOneOfRuleClassifierProvider:
    ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider

  private val inputDoesNotContainAtLeastOneOfRuleClassifier by lazy {
    itemSelectionInputDesNotContainAtLeastOneOfRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMatches_emptyAnswer_emptyInput_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds())

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds(),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // There's nothing to miss from the input so this is never a match.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_emptyAnswer_singletonInput_returnsTrue() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds(),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // Answer is missing 'test1'.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_singletonAnswer_emptyInput_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds())

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // There's nothing to miss from the input so this is never a match.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_singletonAnswer_singletonInput_different_returnsTrue() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test2"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // Answer is missing 'test1'.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_singletonAnswer_singletonInput_same_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // Answer is contains 'test1' (the only input element, so none of input's elements are absent).
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_answerWithTwoElems_inputWithTwoElems_distinct_returnsTrue() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1", "test2"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test3", "test4"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of input's elements are absent.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_answerWithTwoElems_inputWithTwoElems_oneCommon_returnsTrue() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1", "test2"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test4"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // 'test2' is still missing.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_answerWithTwoElems_inputWithTwoElems_bothCommon_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1", "test2"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test2"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of the input's elements are present in the answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_answerIsSubset_returnsTrue() {
    val inputs = mapOf(
      "x" to createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4")
    )

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test2", "test4"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // 'test3' is still missing in the answer.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_answerIsSubset_diffOrders_returnsTrue() {
    val inputs = mapOf(
      "x" to createSetOfTranslatableHtmlContentIds("test2", "test4", "test1", "test3")
    )

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test4", "test1", "test2"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // 'test3' is still missing in the answer.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_answerHasMultipleCommonToInput_returnsTrue() {
    val inputs = mapOf(
      "x" to createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4")
    )

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test2", "test4", "test5", "test6"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // 'test3' is still missing in the answer.
    assertThat(matches).isTrue()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_inputIsSubset_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test2", "test3", "test4"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4", "test5"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of the input's elements are present in the answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_inputIsSubset_diffOrder_returnsFalse() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test4", "test2", "test3"))

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test2", "test3", "test4", "test5", "test1"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of the input's elements are present in the answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_allMatch_returnsFalse() {
    val inputs = mapOf(
      "x" to createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4")
    )

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of the input's elements are present in the answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_multiElemAnswer_multiElemInput_allMatch_diffOrder_returnsFalse() {
    val inputs = mapOf(
      "x" to createSetOfTranslatableHtmlContentIds("test2", "test4", "test3", "test1")
    )

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = createSetOfTranslatableHtmlContentIds("test4", "test1", "test3", "test2"),
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    // All of the input's elements are present in the answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testMatches_inputIsMissing_throwsException() {
    val inputs = mapOf("y" to createSetOfTranslatableHtmlContentIds("test1"))

    val exception = assertThrows<IllegalStateException> {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = createSetOfTranslatableHtmlContentIds("test1"),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testMatches_inputHasTheWrongType_throwsException() {
    val inputs = mapOf("x" to createInt(value = 0))

    val exception = assertThrows<IllegalStateException> {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = createSetOfTranslatableHtmlContentIds("test1"),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type SET_OF_TRANSLATABLE_HTML_CONTENT_IDS")
  }

  @Test
  fun testMatches_answerHasTheWrongType_throwsException() {
    val inputs = mapOf("x" to createSetOfTranslatableHtmlContentIds("test1"))

    val exception = assertThrows<IllegalStateException> {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = createInt(value = 0),
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected answer to be of type SET_OF_TRANSLATABLE_HTML_CONTENT_IDS")
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProviderTest_TestApplicationComponent // ktlint-disable max-line-length
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

    fun inject(test: ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProviderTest)
  }
}
