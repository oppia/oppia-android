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
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.StringList
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)

class ItemSelectionInputContainsAtLeastOneOfRuleClassifierProviderTest {
  private val ITEM_SELECTION_SET_5 = createStringList(
    StringList.newBuilder()
      .addHtml("test1")
      .addHtml("test2")
      .addHtml("test3")
      .addHtml("test4")
      .addHtml("test5")
      .build()
  )

  private val ITEM_SELECTION_SET_SUBSET = createStringList(
    StringList.newBuilder().addHtml("test1")
      .build()
  )

  private val ITEM_SELECTION_SET_ONE_ELEMENT_PRESENT = createStringList(
    StringList.newBuilder().addHtml("test1").addHtml("test6")
      .build()
  )

  private val ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_NO_EXTRA_ELEMENT = createStringList(
    StringList.newBuilder().addHtml("test1").addHtml("test2")
      .build()
  )

  private val ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_WITH_EXTRA_ELEMENT = createStringList(
    StringList.newBuilder().addHtml("test1").addHtml("test2").addHtml("test6")
      .build()
  )

  private val ITEM_SELECTION_SET_EMPTY = createStringList(
    StringList.newBuilder()
      .build()
  )

  private val ITEM_SELECTION_SET_EXCLUSIVE = createStringList(
    StringList.newBuilder().addHtml("test6")
      .build()
  )

  @Inject
  internal lateinit var itemSelectionInputContainsAtLeastOneOfRuleClassifierProvider:
    ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider

  private val inputContainsAtLeastOneOfRuleClassifier by lazy {
    itemSelectionInputContainsAtLeastOneOfRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testItemSet_setAnswer_inputIsASubset_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_SUBSET)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputHasOneElementInSet_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_ONE_ELEMENT_PRESENT)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputHasTwoElementsInSetNoneExtra_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_NO_EXTRA_ELEMENT)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputHasTwoElementsInSetOneExtra_answerContainsInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_WITH_EXTRA_ELEMENT)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testItemSet_setAnswer_inputIsEmptySet_answerDoesNotContainInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_EMPTY)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testItemSet_setAnswer_inputIsExclusiveOfSet_answerDoesNotContainInput() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_EXCLUSIVE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  private fun createStringList(value: StringList): InteractionObject {
    return InteractionObject.newBuilder().setSetOfHtmlString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputContainsAtLeastOneOfRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: ItemSelectionInputContainsAtLeastOneOfRuleClassifierProviderTest)
  }
}
