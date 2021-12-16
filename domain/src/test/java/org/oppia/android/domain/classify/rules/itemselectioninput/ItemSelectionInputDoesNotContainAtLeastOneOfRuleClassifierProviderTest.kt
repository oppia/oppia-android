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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createInt
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createSetOfTranslatableHtmlContentIds
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputDoesNotContainAtLeastOneOfRuleClassifierProviderTest {

  private val ITEM_SET_12345 =
    createSetOfTranslatableHtmlContentIds("test1", "test2", "test3", "test4", "test5")
  private val ITEM_SET_1 = createSetOfTranslatableHtmlContentIds("test1")
  private val ITEM_SET_16 = createSetOfTranslatableHtmlContentIds("test1", "test6")
  private val ITEM_SET_12 = createSetOfTranslatableHtmlContentIds("test1", "test2")
  private val ITEM_SET_126 = createSetOfTranslatableHtmlContentIds("test1", "test2", "test6")
  private val ITEM_SET_EMPTY = createSetOfTranslatableHtmlContentIds()
  private val ITEM_SET_6 = createSetOfTranslatableHtmlContentIds("test6")
  private val DIFFERENT_INTERACTION_OBJECT_TYPE = createInt(value = 0)

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
  fun testItemSet_setAnswer_inputIsASubset_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SET_1)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_setAnswer_inputHasOneElementInSet_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SET_16)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_setAnswer_inputHasTwoElementsInSetNoneExtra_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SET_12)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_setAnswer_inputHasTwoElementsInSetOneExtra_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SET_126)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_setAnswer_inputIsEmptySet_answerDoesNotContainInput() {
    val inputs = mapOf("x" to ITEM_SET_EMPTY)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputIsExclusiveOfSet_answerDoesNotContainInput() {
    val inputs = mapOf("x" to ITEM_SET_6)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12345,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswerIsEmpty_inputIsNonEmpty_answerDoesNotContainInput() {
    val inputs = mapOf("x" to ITEM_SET_12345)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_EMPTY,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputIsASuperset_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SET_12345)

    val matches = inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SET_12,
      inputs = inputs,
      classificationContext = ClassificationContext()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_inputIsMissing_throwsException() {
    val inputs = mapOf("y" to ITEM_SET_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = ITEM_SET_12345,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testItemSet_inputHasTheWrongType_throwsException() {
    val inputs = mapOf("x" to DIFFERENT_INTERACTION_OBJECT_TYPE)

    val exception = assertThrows(IllegalStateException::class) {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = ITEM_SET_12345,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type SET_OF_TRANSLATABLE_HTML_CONTENT_IDS")
  }

  @Test
  fun testItemSet_answerHasTheWrongType_throwsException() {
    val inputs = mapOf("x" to ITEM_SET_12345)

    val exception = assertThrows(IllegalStateException::class) {
      inputDoesNotContainAtLeastOneOfRuleClassifier.matches(
        answer = DIFFERENT_INTERACTION_OBJECT_TYPE,
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
