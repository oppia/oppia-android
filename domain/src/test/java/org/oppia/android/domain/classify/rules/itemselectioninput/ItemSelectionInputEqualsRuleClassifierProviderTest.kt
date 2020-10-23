package org.oppia.android.domain.classify.rules.itemselectioninput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.StringList
import org.oppia.android.domain.classify.RuleClassifier
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ItemSelectionInputEqualsRuleClassifierProviderTest {

  @Inject
  internal lateinit var itemSelectionInputEqualsRuleClassifierProvider:
    ItemSelectionInputEqualsRuleClassifierProvider

  private val inputIsEquivalentToRuleClassifierProvider by lazy {
    itemSelectionInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  private val TEST_ITEM_SELECTION_SET_LOWERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO"))
    .build()

  private val TEST_ITEM_SELECTION_SET_UPPERCASE = InteractionObject.newBuilder()
    .setSetOfHtmlString(StringList.newBuilder().addHtml("YES").addHtml("NO"))
    .build()

  @Test
  fun testEquals_lowercaseAlphabetsAnswer_withlowercaseAlphabetsAnswerInput_bothValuesEquivalent() {
  val matches =
    inputIsEquivalentToRuleClassifierProvider.matches(
      inputs = mapOf("x" to TEST_ITEM_SELECTION_SET_LOWERCASE),
      answer = TEST_ITEM_SELECTION_SET_UPPERCASE
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_lowercaseAlphabetsAnswer_withuppercaseAlphabetsAnswerInput_valuesNotEquivalent() {
    val matches =
      inputIsEquivalentToRuleClassifierProvider.matches(
        inputs = mapOf("x" to TEST_ITEM_SELECTION_SET_UPPERCASE),
        answer = TEST_ITEM_SELECTION_SET_LOWERCASE
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_uppercaseAlphabetAnswer_withlowercaseAlphabetsAnswerInput_valuesNotEquivalent() {
    val matches =
      inputIsEquivalentToRuleClassifierProvider.matches(
        inputs = mapOf("x" to TEST_ITEM_SELECTION_SET_LOWERCASE),
        answer = TEST_ITEM_SELECTION_SET_LOWERCASE
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEquals_uppercaseAlphabetsAnswer_withuppercaseAlphabetsAnswerInput_bothValuesEquivalent() {
    val matches =
      inputIsEquivalentToRuleClassifierProvider.matches(
        inputs = mapOf("x" to TEST_ITEM_SELECTION_SET_UPPERCASE),
        answer = TEST_ITEM_SELECTION_SET_LOWERCASE
      )

    assertThat(matches).isTrue()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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
