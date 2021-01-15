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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 0)

  private val ITEM_SET_1_AB =
    InteractionObjectTestBuilder.createHtmlStringList("item a", "item b")

  private val ITEM_SET_1_ABC =
    InteractionObjectTestBuilder.createHtmlStringList("item a", "item b", "item c")

  private val ITEM_SET_1_A =
    InteractionObjectTestBuilder.createHtmlStringList("item a")

  private val ITEM_SET_2_ITEM_2 =
    InteractionObjectTestBuilder.createHtmlStringList("item 2")

  private val ITEM_SET_3_ITEM_3 =
    InteractionObjectTestBuilder.createHtmlStringList("item 3")

  private val SET_LIST_ITEMS_AB_ITEM_2_ITEM_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(ITEM_SET_1_AB, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)
    )

  private val SET_LIST_ITEMS_ABC_ITEM_2_ITEM_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(ITEM_SET_1_ABC, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)
    )

  private val SET_LIST_ITEM_2_ITEM_3_ITEMS_AB =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3, ITEM_SET_1_AB)
    )

  private val SET_LIST_ITEM_A_ITEM_2_ITEM_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(ITEM_SET_1_A, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)
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
        answer = SET_LIST_ITEMS_AB_ITEM_2_ITEM_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Expected input value to be of type LIST_OF_SETS_OF_HTML_STRING not NON_NEGATIVE_INT"
      )
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_sameValue_bothValuesMatch_failsCorrectly() {
    val inputs = mapOf("x" to SET_LIST_ITEMS_AB_ITEM_2_ITEM_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = SET_LIST_ITEMS_AB_ITEM_2_ITEM_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to SET_LIST_ITEMS_AB_ITEM_2_ITEM_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = SET_LIST_ITEM_A_ITEM_2_ITEM_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_differentOrder_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to SET_LIST_ITEM_2_ITEM_3_ITEMS_AB)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = SET_LIST_ITEM_A_ITEM_2_ITEM_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByTwoElements_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to SET_LIST_ITEMS_ABC_ITEM_2_ITEM_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = SET_LIST_ITEM_A_ITEM_2_ITEM_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to SET_LIST_ITEMS_AB_ITEM_2_ITEM_3)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = SET_LIST_ITEMS_AB_ITEM_2_ITEM_3,
        inputs = inputs
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
