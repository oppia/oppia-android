package org.oppia.android.domain.classify.rules.itemselectioninput

import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.StringList
import javax.inject.Inject
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [ItemSelectionInputContainsAtLeastOneOfRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)

class ItemSelectionInputContainsAtLeastOneOfRuleClassifierProviderTest {
  private val ITEM_SELECTION_SET_2_LOWERCASE = createStringList(StringList.newBuilder().addHtml("test1").addHtml("test2").build())
  private val ITEM_SELECTION_SET_2_UPPERCASE = createStringList(StringList.newBuilder().addHtml("TEST1").addHtml("TEST2").build())

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
  fun testEquals_lowerCaseAnswer_lowerCaseInput_bothValuesMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_2_LOWERCASE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_2_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowerCaseAnswer_upperCaseInput_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_2_UPPERCASE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_2_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEquals_upperCaseAnswer_upperCaseInput_bothValuesMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_2_UPPERCASE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_2_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_upperCaseAnswer_lowerCaseInput_bothValuesDoNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_2_LOWERCASE)

    val matches = inputContainsAtLeastOneOfRuleClassifier.matches(
      answer = ITEM_SELECTION_SET_2_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  private fun createStringList(value: StringList): InteractionObject {
    return InteractionObject.newBuilder().setSetOfHtmlString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputContainsAtLeastOneOfRuleClassifierProviderTest_TestApplicationComponent.builder()
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
