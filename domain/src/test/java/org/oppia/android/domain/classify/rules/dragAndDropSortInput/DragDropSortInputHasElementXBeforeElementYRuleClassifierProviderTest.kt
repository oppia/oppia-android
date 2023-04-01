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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslatableHtmlContentId
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DragDropSortInputHasElementXBeforeElementYClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DragDropSortInputHasElementXBeforeElementYRuleClassifierProviderTest {

  private val VALID_CONTENT_ID_1 = createTranslatableHtmlContentId(contentId = "content_id_1")
  private val VALID_CONTENT_ID_2 = createTranslatableHtmlContentId(contentId = "content_id_2")
  private val INVALID_CONTENT_ID = createTranslatableHtmlContentId(contentId = "invalid_content_id")
  private val NON_NEGATIVE_VALUE_1 = createNonNegativeInt(value = 1)
  private val LIST_OF_SETS_OF_CONTENT_IDS =
    createListOfSetsOfTranslatableHtmlContentIds(
      listOf("content_id_1"), listOf("content_id_2"), listOf("content_id_3")
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
      "x" to NON_NEGATIVE_VALUE_1,
      "y" to NON_NEGATIVE_VALUE_1
    )

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_HTML_CONTENT_ID")
  }

  @Test
  fun testAnswer_nonNegativeInput_testString_xInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_1, "y" to VALID_CONTENT_ID_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_HTML_CONTENT_ID")
  }

  @Test
  fun testAnswer_nonNegativeInput_testString_yInputWithIncorrectType_throwsException() {
    val inputs = mapOf("x" to VALID_CONTENT_ID_2, "y" to NON_NEGATIVE_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_HTML_CONTENT_ID")
  }

  @Test
  fun testAnswer_testString_missingInputX_throwsException() {
    val inputs = mapOf("y" to VALID_CONTENT_ID_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  @Test
  fun testAnswer_nonNegativeInput_missingInputY_throwsException() {
    val inputs = mapOf("x" to VALID_CONTENT_ID_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'y' but had: [x]")
  }

  @Test
  fun testAnswer_bothInputsMissing_throwsException() {
    val inputs = mapOf("z" to VALID_CONTENT_ID_2)

    val exception = assertThrows(IllegalStateException::class) {
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [z]")
  }

  @Test
  fun testAnswer_elementXAfterElementY_orderedIncorrectly() {
    val inputs = mapOf("x" to VALID_CONTENT_ID_2, "y" to VALID_CONTENT_ID_1)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_elementX_invalidElementY_orderedIncorrectly() {
    val inputs = mapOf("y" to INVALID_CONTENT_ID, "x" to VALID_CONTENT_ID_2)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_elementXBeforeElementY_orderedCorrectly() {
    val inputs = mapOf("x" to VALID_CONTENT_ID_1, "y" to VALID_CONTENT_ID_2)

    val matches =
      hasElementXBeforeElementYRuleClassifier.matches(
        answer = LIST_OF_SETS_OF_CONTENT_IDS,
        inputs = inputs,
        classificationContext = ClassificationContext()
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
