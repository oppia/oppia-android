package org.oppia.domain.classify.rules.dragAndDropSortInput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.domain.classify.RuleClassifier
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
  private val ITEM_SET_1_A = listOf("item a")
  private val ITEM_SET_1_AB = listOf("item a", "item b")
  private val ITEM_SET_2_ITEM_2 = listOf("item 2")
  private val ITEM_SET_3_ITEM_3 = listOf("item 3")
  private val ITEM_SET_4_INVALID_AB = listOf("item invalid a", "item invalid b")
  private val LIST_OF_SETS_123 =
    createListOfSetsOfHtmlStrings(ITEM_SET_1_AB, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)
  private val LIST_OF_SETS_213 =
    createListOfSetsOfHtmlStrings(ITEM_SET_2_ITEM_2, ITEM_SET_1_AB, ITEM_SET_3_ITEM_3)
  private val LIST_OF_SETS_243 =
    createListOfSetsOfHtmlStrings(ITEM_SET_2_ITEM_2, ITEM_SET_4_INVALID_AB, ITEM_SET_3_ITEM_3)
  private val LIST_OF_SETS_21 = createListOfSetsOfHtmlStrings(ITEM_SET_2_ITEM_2, ITEM_SET_1_AB)
  private val LIST_OF_SETS_1A23 =
    createListOfSetsOfHtmlStrings(ITEM_SET_1_A, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)

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
    val inputs = mapOf("x" to LIST_OF_SETS_123)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentOrder_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_213)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentList_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_243)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_21)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_elementDifferentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_1A23)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_123)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_123, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  private fun createListOfSetsOfHtmlStrings(vararg items: List<String>): InteractionObject {
    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(items.map { createHtmlStringList(it) })
      .build()

    return InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()
  }

  private fun createHtmlStringList(items: List<String>): StringList {
    return StringList.newBuilder().addAllHtml(items).build()
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
