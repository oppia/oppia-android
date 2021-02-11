package org.oppia.android.domain.classify.rules.itemselectioninput

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
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputIsProperSubsetOfRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputIsProperSubsetOfRuleClassifierProviderTest() {

  private val ITEM_SELECTION_12345 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2", "test3", "test4", "test5")
    )

  private val ITEM_SELECTION_1 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1")
    )

  private val ITEM_SELECTION_16 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test6")
    )

  private val ITEM_SELECTION_12 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2")
    )

  private val ITEM_SELECTION_126 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2", "test6")
    )

  private val ITEM_SELECTION_NONE =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList()
    )

  private val ITEM_SELECTION_6 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test6")
    )

  private val ITEM_SELECTION_INVAILD =
    InteractionObjectTestBuilder.createString(
      value = "test string"
    )

  @Inject
  internal lateinit var itemSelectionInputIsProperSubsetOfRuleClassifierProvider:
    ItemSelectionInputIsProperSubsetOfRuleClassifierProvider

  private val inputContainsAtLeastOneOfRuleClassifier by lazy {
    itemSelectionInputIsProperSubsetOfRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testIsProperSubset_withInput1_forAnswer12345_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_1)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_12345,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInput16_forAnswer12345_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_16)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_12345,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInput12345_forAnswer12_returnsTrue() {
    val inputs = mapOf("x" to ITEM_SELECTION_12345)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_12,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testIsProperSubset_withInput12345_forAnswer126_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_12345)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_126,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withEmptyInput_forAnswer16_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_NONE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_16,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInput1_forEmptyAnswer_returnsTrue() {
    val inputs = mapOf("x" to ITEM_SELECTION_1)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_NONE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testIsProperSubset_withEmptyInput_forEmptyAnswer_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_NONE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_NONE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInput1_forAnswer6_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_1)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_6,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInput12345_forAnswer12345_returnsFalse() {
    val inputs = mapOf("x" to ITEM_SELECTION_12345)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_12345,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testIsProperSubset_withInvalidInput_forAnswer12345_throwsException() {
    val inputs = mapOf("x" to ITEM_SELECTION_INVAILD)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsAtLeastOneOfRuleClassifier.matches(
        answer = ITEM_SELECTION_12345,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type SET_OF_HTML_STRING not NORMALIZED_STRING")
  }

  @Test
  fun testIsProperSubset_withInput12345_forInvalidAnswer_throwsException() {
    val inputs = mapOf("x" to ITEM_SELECTION_12345)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsAtLeastOneOfRuleClassifier.matches(
        answer = ITEM_SELECTION_INVAILD,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected answer to be of type SET_OF_HTML_STRING not NORMALIZED_STRING")
  }

  @Test
  fun testIsProperSubset_missingInputX_throwsException() {
    val inputs = mapOf("a" to ITEM_SELECTION_12345)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsAtLeastOneOfRuleClassifier.matches(
        answer = ITEM_SELECTION_12345,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [a]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputIsProperSubsetOfRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ItemSelectionInputIsProperSubsetOfRuleClassifierProviderTest)
  }
}
