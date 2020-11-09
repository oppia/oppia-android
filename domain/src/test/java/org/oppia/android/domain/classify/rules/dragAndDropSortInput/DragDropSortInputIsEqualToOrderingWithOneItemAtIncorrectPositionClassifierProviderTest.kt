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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest {

  private val NON_NEGATIVE_VALUE_TEST_0 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 0)

  private val SET_ITEM_A = InteractionObjectTestBuilder.createHtmlStringList("item a")

  private val SET_ITEM_A_B = InteractionObjectTestBuilder.createHtmlStringList(
    "item a", "item b"
  )

  private val SET_ITEM_A_B_C = InteractionObjectTestBuilder.createHtmlStringList(
    "item a", "item b", "item c"
  )

  private val SET_ITEM_2 = InteractionObjectTestBuilder.createHtmlStringList("item 2")

  private val SET_ITEM_3 = InteractionObjectTestBuilder.createHtmlStringList("item 3")

  private val LIST_OF_SETS_AB_2_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_A_B, SET_ITEM_2, SET_ITEM_3)
    )

  private val LIST_OF_SETS_ABC_2_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_A_B_C, SET_ITEM_2, SET_ITEM_3)
    )

  private val LIST_OF_SETS_2_3_AB =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_2, SET_ITEM_3, SET_ITEM_A_B)
    )

  private val LIST_OF_SETS_A_2_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_A, SET_ITEM_2, SET_ITEM_3)
    )

  @Inject
  internal lateinit var dragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider: // ktlint-disable max-line-length
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
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_0)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_AB_2_3,
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
    val inputs = mapOf("x" to LIST_OF_SETS_AB_2_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_AB_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_AB_2_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_A_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_differentOrder_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to LIST_OF_SETS_2_3_AB)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_A_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByTwoElements_bothValuesDoNotMatch_failsCorrectly() { // ktlint-disable max-line-length
    val inputs = mapOf("x" to LIST_OF_SETS_ABC_2_3)

    val matches =
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_A_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_AB_2_3)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemIncorrectClassifier.matches(
        answer = LIST_OF_SETS_AB_2_3,
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

    fun inject(
      test:
        DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest
    )
  }
}
