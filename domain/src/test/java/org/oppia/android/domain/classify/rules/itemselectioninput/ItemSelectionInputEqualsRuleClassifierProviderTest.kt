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
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [ItemSelectionInputEqualsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputEqualsRuleClassifierProviderTest {

  private val ITEM_SELECTION_SET_LOWERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO")).build()

  private val ITEM_SELECTION_SET_UPPERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO")).build()

  private val ITEM_SELECTION_SET_MIXEDLOWERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO")).build()

  private val ITEM_SELECTION_SET_MIXEDUPPERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO")).build()

  private val ITEM_SELECTION_SET_NON_NEGATIVE_VALUE_3 =
    InteractionObject.newBuilder()
      .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO")).build()

  @Inject
  internal lateinit var itemSelectionInputEqualsRuleClassifierProvider:
    ItemSelectionInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    itemSelectionInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testEquals_lowercaseAlphabetsAnswer_withlowercaseAlphabetsAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAlphabetsAnswer_withuppercaseAlphabetsAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_uppercaseAlphabetAnswer_withlowercaseAlphabetsAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_UPPERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_uppercaseAlphabetsAnswer_withuppercaseAlphabetsAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_LOWERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_mixedlowercaseAnswer_withmixeduppercaseAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_MIXEDLOWERCASE)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_MIXEDUPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withuppercaseAlphabetsAnswerInput_bothValuesMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_nonNegativeAnswer_withlowercaseAlphabetsAnswerInput_bothValuesNotMatch() {
    val inputs = mapOf("x" to ITEM_SELECTION_SET_NON_NEGATIVE_VALUE_3)

    val matches =
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testlowercaseAlphabetsAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to ITEM_SELECTION_SET_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = ITEM_SELECTION_SET_LOWERCASE, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

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

  private fun setUpTestApplicationComponent() {
    DaggerItemSelectionInputEqualsRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(test: ItemSelectionInputEqualsRuleClassifierProviderTest)
  }
}
