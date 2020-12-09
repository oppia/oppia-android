package org.oppia.android.domain.classify.rules.itemselectioninput

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ItemSelectionInputIsProperSubsetOfRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputIsProperSubsetOfRuleClassifierProviderTest(){

  private val ITEM_SELECTION_SET_5 =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2", "test3", "test4", "test5")
    )

  private val ITEM_SELECTION_SET_SUBSET =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1")
    )

  private val ITEM_SELECTION_SET_ONE_ELEMENT_PRESENT =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test6")
    )

  private val ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_NO_EXTRA_ELEMENT =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2")
    )

  private val ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_WITH_EXTRA_ELEMENT =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test1", "test2", "test6")
    )

  private val ITEM_SELECTION_SET_EMPTY =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList()
    )

  private val ITEM_SELECTION_SET_EXCLUSIVE =
    InteractionObjectTestBuilder.createHtmlStringListInteractionObject(
      InteractionObjectTestBuilder
        .createHtmlStringList("test6")
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
  fun answerSetMoreThanInputSet_isSubset_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_SUBSET)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun answerSetMoreThanInputSet_notSubset_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_ONE_ELEMENT_PRESENT)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun answerSetLessThanInputSet_isSubset_returnsTrue(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_5)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_NO_EXTRA_ELEMENT,
      inputs = inputs
    )

    Truth.assertThat(matches).isTrue()
  }

  @Test
  fun answerSetLessThanInputSet_notSubset_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_5)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_TWO_ELEMENTS_PRESENT_WITH_EXTRA_ELEMENT,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun inputSetEmpty_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_EMPTY)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_ONE_ELEMENT_PRESENT,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun answerSetSameAsInputSet_notSubset_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_SUBSET)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_EXCLUSIVE,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
  }

  @Test
  fun answerSetSameAsInputSet_isSubset_returnsFalse(){
    val inputs = mapOf("x" to ITEM_SELECTION_SET_5)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_5,
      inputs = inputs
    )

    Truth.assertThat(matches).isFalse()
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