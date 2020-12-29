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

/** Tests for [DragDropSortInputHasElementXBeforeElementYClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputHasElementXBeforeElementYRuleClassifierProviderTest {

  private val STRING_VALUE_1 =
    InteractionObjectTestBuilder.createString(value = "test item 1")

  private val STRING_VALUE_2 =
    InteractionObjectTestBuilder.createString(value = "test item 2")

  private val STRING_VALUE_3 =
    InteractionObjectTestBuilder.createString(value = "test item invalid")

  private val NON_NEGATIVE_VALUE_4 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 1)

  private val LIST_OF_SETS_OF_HTML_STRING_VALUE =
    InteractionObjectTestBuilder.createListOfSetsOfHtmlStrings(
      listOf(
        InteractionObjectTestBuilder.createHtmlStringList("test item 1"),
        InteractionObjectTestBuilder.createHtmlStringList("test item 2"),
        InteractionObjectTestBuilder.createHtmlStringList("test item 3")
      )
    )

  @Inject
  internal lateinit var dragDropSortInputHasElementXBeforeElementYClassifierProvider:
    DragDropSortInputHasElementXBeforeElementYClassifierProvider

  private val hasElementXBeforeElementYRuleClassifier: RuleClassifier by lazy {
    dragDropSortInputHasElementXBeforeElementYClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_nonNegativeInput_bothInputsWithIncorrectTypes_throwsException() {
    val inputs = mapOf(
      "x" to NON_NEGATIVE_VALUE_4,
      "y" to NON_NEGATIVE_VALUE_4
    )

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING not NON_NEGATIVE_INT")
  }

  @Test
  fun testAnswer_nonNegativeInput_testString_xInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_4, "y" to STRING_VALUE_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING not NON_NEGATIVE_INT")
  }

  @Test
  fun testAnswer_nonNegativeInput_testString_yInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE_2, "y" to NON_NEGATIVE_VALUE_4)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING not NON_NEGATIVE_INT")
  }

  @Test
  fun testAnswer_testString_missingInputX_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  @Test
  fun testAnswer_nonNegativeInput_missingInputY_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'y' but had: [x]")
  }

  @Test
  fun testAnswer_bothInputsMissing_throwsException() {
    val inputs = mapOf("z" to STRING_VALUE_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [z]")
  }

  @Test
  fun testAnswer_elementXAfterElementY_orderedIncorrectly() {
    val inputs = mapOf("x" to STRING_VALUE_2, "y" to STRING_VALUE_1)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_elementX_invalidElementY_orderedIncorrectly() {
    val inputs = mapOf("y" to STRING_VALUE_3, "x" to STRING_VALUE_2)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_elementXBeforeElementY_orderedCorrectly() {
    val inputs = mapOf("x" to STRING_VALUE_1, "y" to STRING_VALUE_2)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_HTML_STRING_VALUE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDragDropSortInputHasElementXBeforeElementYRuleClassifierProviderTest_TestApplicationComponent // ktlint-disable max-line-length
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

    fun inject(test: DragDropSortInputHasElementXBeforeElementYRuleClassifierProviderTest)
  }
}
