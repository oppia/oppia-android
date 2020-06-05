package org.oppia.domain.classify.rules.dragAndDropSortInput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.domain.classify.RuleClassifier
import org.robolectric.annotation.Config

/** Tests for [DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest {
  private val NON_NEGATIVE_VALUE_0 = createNonNegativeInt(value = 0)
  private val ITEM_SET_1_AB = listOf("item a", "item b")
  private val ITEM_SET_1_A = listOf("item a")
  private val ITEM_SET_2_ITEM_2 = listOf("item 2")
  private val ITEM_SET_3_ITEM_3 = listOf("item 3")
  private val ITEM_SET_4_INVALID_AB = listOf("item invalid a", "item invalid b")
  private val LIST_OF_SETS_123 =
    createListOfSetsOfHtmlStrings(ITEM_SET_1_AB, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)
  private val LIST_OF_SETS_132 =
    createListOfSetsOfHtmlStrings(ITEM_SET_1_AB, ITEM_SET_3_ITEM_3, ITEM_SET_2_ITEM_2)
  private val LIST_OF_SETS_1A23 =
    createListOfSetsOfHtmlStrings(ITEM_SET_1_A, ITEM_SET_2_ITEM_2, ITEM_SET_3_ITEM_3)

  @Inject
  internal lateinit var dragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider:
    DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider

  private val isEqualToOrderingWithOneItemAtIncorrectPositionClassifier: RuleClassifier by lazy {
    dragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_nonNegativeInput_inputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_0)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemAtIncorrectPositionClassifier.matches(
        answer = LIST_OF_SETS_123,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type LIST_OF_SETS_OF_HTML_STRING not NON_NEGATIVE_INT")
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_sameValue_bothValuesMatch_failsCorrectly() {
    val inputs = mapOf("x" to LIST_OF_SETS_123)

    val matches =
      isEqualToOrderingWithOneItemAtIncorrectPositionClassifier.matches(
        answer = LIST_OF_SETS_123,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differByOneElement_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_123)

    val matches =
      isEqualToOrderingWithOneItemAtIncorrectPositionClassifier.matches(
        answer = LIST_OF_SETS_1A23,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_123)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingWithOneItemAtIncorrectPositionClassifier.matches(
        answer = LIST_OF_SETS_123,
        inputs = inputs
      )
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

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: DragDropSortInputIsEqualToOrderingWithOneItemAtIncorrectPositionClassifierProviderTest)
  }
}
