package org.oppia.domain.classify.rules.dragAndDropSortInput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [DragDropSortInputIsEqualToOrderingClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DragDropSortInputIsEqualToOrderingClassifierProviderTest {
  private val LIST_OF_SETS_OF_HTML_STRING_VALUE_1 = createListOfSetsOfHtmlStrings("this","is","a","test")
  private val LIST_OF_SETS_OF_HTML_STRING_VALUE_2 = createListOfSetsOfHtmlStrings("is","this","a","test")
  private val LIST_OF_SETS_OF_HTML_STRING_VALUE_3 = createListOfSetsOfHtmlStrings("this","is","a","failed","test")

  @Inject
  internal lateinit var dragDropSortInputIsEqualToOrderingClassifierProvider: DragDropSortInputIsEqualToOrderingClassifierProvider

  private val isEqualToOrderingClassifierProvider:RuleClassifier by lazy {
    dragDropSortInputIsEqualToOrderingClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_sameValue_bothValuesMatch() {
    val inputs = mapOf( "x" to LIST_OF_SETS_OF_HTML_STRING_VALUE_1)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_OF_HTML_STRING_VALUE_1, inputs = inputs)

    Truth.assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentValue_bothValuesDoNotMatch() {
    val inputs = mapOf( "x" to LIST_OF_SETS_OF_HTML_STRING_VALUE_2)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_OF_HTML_STRING_VALUE_1, inputs = inputs)

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_differentLength_bothValuesDoNotMatch() {
    val inputs = mapOf( "x" to LIST_OF_SETS_OF_HTML_STRING_VALUE_3)

    val matches =
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_OF_HTML_STRING_VALUE_1, inputs = inputs)

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer_testLisOfSetsOfHtmlString_incorrectInputMap_throwsException() {
    val inputs = mapOf("y" to LIST_OF_SETS_OF_HTML_STRING_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      isEqualToOrderingClassifierProvider.matches(answer = LIST_OF_SETS_OF_HTML_STRING_VALUE_1, inputs = inputs)
    }

    Truth.assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }


  private fun createListOfSetsOfHtmlStrings(vararg items: String): InteractionObject {
    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(items.map { createHtmlStringList(it.split("")) })
      .build()

    return InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()
  }

  private fun createHtmlStringList(items: List<String>): StringList {
    return StringList.newBuilder().addAllHtml(items).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDragDropSortInputIsEqualToOrderingClassifierProviderTest_TestApplicationComponent.builder()
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