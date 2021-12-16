package org.oppia.android.domain.classify.rules.dragAndDropSortInput

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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createNonNegativeInt
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_0 = createNonNegativeInt(value = 0)
  private val ITEM_SET_1_ITEMS_12 = listOf("content_id_1", "content_id_2")
  private val ITEM_SET_1_ITEMS_123 = listOf("content_id_1", "content_id_2", "content_id_3")
  private val ITEM_SET_1_ITEM_1 = listOf("content_id_1")
  private val ITEM_SET_2_ITEM_4 = listOf("content_id_4")
  private val ITEM_SET_3_ITEM_5 = listOf("content_id_5")

  private val LIST_OF_SETS_12_4_5 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_1_ITEMS_12, ITEM_SET_2_ITEM_4, ITEM_SET_3_ITEM_5
    )
  private val LIST_OF_SETS_123_4_5 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_1_ITEMS_123, ITEM_SET_2_ITEM_4, ITEM_SET_3_ITEM_5
    )
  private val LIST_OF_SETS_4_5_12 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_2_ITEM_4, ITEM_SET_3_ITEM_5, ITEM_SET_1_ITEMS_12
    )
  private val LIST_OF_SETS_1_4_5 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_1_ITEM_1, ITEM_SET_2_ITEM_4, ITEM_SET_3_ITEM_5
    )

  @Inject
  internal lateinit var
  dragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider:
    DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider

  private val isEqualToOrderingWithOneItemIncorrectClassifier: RuleClassifier by lazy {
    dragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider
      .createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_0)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_12_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type LIST_OF_SETS_OF_TRANSLATABLE_HTML_CONTENT_IDS"
      )
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_sameValue_bothValuesMatch_failsCorrectly() {
    val inputs = mapOf("x" to LIST_OF_SETS_12_4_5)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_12_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_12_4_5)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_1_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_differentOrder_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to LIST_OF_SETS_4_5_12)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_1_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByTwoElements_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to LIST_OF_SETS_123_4_5)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_1_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_12_4_5)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_12_4_5,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerDragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest_TestApplicationComponent.builder() // ktlint-disable max-line-length
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(
      test:
        DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest
    )
  }
}
