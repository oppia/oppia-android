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

/** Tests for [DragDropSortInputIsEqualToOrderingClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingClassifierProviderTest {

  private val SET_ITEM_A = InteractionObjectTestBuilder.createHtmlStringList("item a")

  private val SET_ITEM_A_B = InteractionObjectTestBuilder.createHtmlStringList(
    "item a", "item b"
  )

  private val SET_ITEM_2 = InteractionObjectTestBuilder.createHtmlStringList("item 2")

  private val SET_ITEM_3 = InteractionObjectTestBuilder.createHtmlStringList("item 3")

  private val SET_ITEM_INVALID_A_B = InteractionObjectTestBuilder.createHtmlStringList(
    "item invalid a", "item invalid b"
  )

  private val LIST_OF_SETS_1_2_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_A_B, SET_ITEM_2, SET_ITEM_3)
    )

  private val LIST_OF_SETS_2_1_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_2, SET_ITEM_A_B, SET_ITEM_3)
    )

  private val LIST_OF_SETS_2_4_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_2, SET_ITEM_INVALID_A_B, SET_ITEM_3)
    )

  private val LIST_OF_SETS_2_1 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_2, SET_ITEM_A_B)
    )

  private val LIST_OF_SETS_A_2_3 =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(SET_ITEM_A, SET_ITEM_2, SET_ITEM_3)
    )

  @Inject
  internal lateinit var dragDropSortInputIsEqualToOrderingClassifierProvider:
    DragDropSortInputIsEqualToOrderingClassifierProvider

  private val isEqualToOrderingClassifierProvider: RuleClassifier by lazy {
    dragDropSortInputIsEqualToOrderingClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_sameValue_bothValuesMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_1_2_3)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentOrder_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_2_1_3)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentList_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_2_4_3)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_2_1)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_elementDifferentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_A_2_3)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_1_2_3)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_1_2_3,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerDragDropSortInputIsEqualToOrderingClassifierProviderTest_TestApplicationComponent
      .builder()
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

    fun inject(test: DragDropSortInputIsEqualToOrderingClassifierProviderTest)
  }
}
