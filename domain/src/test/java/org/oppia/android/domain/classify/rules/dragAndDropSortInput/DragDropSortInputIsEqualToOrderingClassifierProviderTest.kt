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
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DragDropSortInputIsEqualToOrderingClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingClassifierProviderTest {

  private val ITEM_SET_1_ITEM_1 = listOf("content_id_1")
  private val ITEM_SET_1_ITEMS_12 = listOf("content_id_1", "content_id_2")
  private val ITEM_SET_2_ITEM_3 = listOf("content_id_3")
  private val ITEM_SET_3_ITEM_4 = listOf("content_id_4")
  private val ITEM_SET_4_INVALID_12 = listOf("invalid_content_id_1", "invalid_content_id_1")

  private val LIST_OF_SETS_12_3_4 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_1_ITEMS_12, ITEM_SET_2_ITEM_3, ITEM_SET_3_ITEM_4
    )
  private val LIST_OF_SETS_3_12_4 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_2_ITEM_3, ITEM_SET_1_ITEMS_12, ITEM_SET_3_ITEM_4
    )
  private val LIST_OF_SETS_3_INVALID12_4 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_2_ITEM_3, ITEM_SET_4_INVALID_12, ITEM_SET_3_ITEM_4
    )
  private val LIST_OF_SETS_3_12 =
    createListOfSetsOfTranslatableHtmlContentIds(ITEM_SET_2_ITEM_3, ITEM_SET_1_ITEMS_12)
  private val LIST_OF_SETS_1_3_4 =
    createListOfSetsOfTranslatableHtmlContentIds(
      ITEM_SET_1_ITEM_1, ITEM_SET_2_ITEM_3, ITEM_SET_3_ITEM_4
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
    val inputs = mapOf("x" to LIST_OF_SETS_12_3_4)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentOrder_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_3_12_4)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentList_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_3_INVALID12_4)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_3_12)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_elementDifferentLength_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to LIST_OF_SETS_1_3_4)

    val matches =
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_12_3_4)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingClassifierProvider.matches(
        answer = LIST_OF_SETS_12_3_4,
        inputs = inputs,
        classificationContext = ClassificationContext()
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
