package org.oppia.android.domain.classify.rules.textinput

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createNonNegativeInt
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createString
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslatableSetOfNormalizedString
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslationContext
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule

/** Tests for [TextInputContainsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputContainsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_ANSWER_NULL = createString(value = "")
  private val STRING_VALUE_IS_ANSWER = createString(value = "is")
  private val STRING_VALUE_NOT_ANSWER = createString(value = "not")
  private val STRING_VALUE_TEST_ANSWER = createString(value = "this is a test")
  private val NON_NEGATIVE_VALUE_TEST_1 = createNonNegativeInt(value = 1)

  private val TEST_STRING_CONTENT_ID = "test_content_id"
  private val STRING_VALUE_TEST_CONTAINS_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString(
      "this is a test i will break", contentId = TEST_STRING_CONTENT_ID
    )
  private val STRING_VALUE_TEST_AN_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("an answer", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_A_TEST_INPUT_SET =
    createTranslatableSetOfNormalizedString("a test", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_IS_A_INPUT_SET =
    createTranslatableSetOfNormalizedString("is a", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_THIS_IS_INPUT_SET =
    createTranslatableSetOfNormalizedString("this is", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_NULL_INPUT_SET =
    createTranslatableSetOfNormalizedString("", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("this is a test", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_EXTRA_SPACE_INPUT_SET =
    createTranslatableSetOfNormalizedString(
      " this   is  a  test ", contentId = TEST_STRING_CONTENT_ID
    )
  private val STRING_VALUE_TEST_NO_SPACE_INPUT_SET =
    createTranslatableSetOfNormalizedString("thisisatest", contentId = TEST_STRING_CONTENT_ID)
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString(
      "this", "is", "a test", contentId = TEST_STRING_CONTENT_ID
    )

  @Inject
  internal lateinit var textInputContainsRuleClassifierProvider:
    TextInputContainsRuleClassifierProvider

  private val inputContainsRuleClassifier by lazy {
    textInputContainsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAnswer_emptyStringInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches =
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_NULL,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_emptyStringInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_wordStringAnswer_inputWithMultipleMatches_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_IS_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_wordStringAnswer_inputUnmatchingStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_NOT_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtBeginning_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_THIS_IS_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputInMiddle_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_IS_A_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtEnd_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_A_TEST_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringExtraSpacesInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_inputNotInAnswer_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAnswer_nonEmptyStringInput_answerDoesNotContainInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val matches =
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_NULL,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerPartiallyContainsInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_CONTAINS_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_answerPartiallyContainsInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_SET_OF_NORMALIZED_STRING")
  }

  /* Localization-based tests. */

  @Test
  fun testString_inputsWithPortuguese_answerInEnglish_englishContext_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = createString("an answer among many"),
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testString_inputsWithPortuguese_answerInPortuguese_englishContext_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = createString("uma resposta entre muitas"),
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    // A Portuguese answer isn't reocgnized with this translation context.
    assertThat(matches).isFalse()
  }

  @Test
  fun testString_inputsWithPortuguese_answerInEnglish_portugueseContext_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = createString("an answer among many"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // Even though the English string matches, the presence of the Portuguese context should trigger
    // a failure for an English answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testString_inputsWithPortuguese_answerInPortuguese_portugueseContext_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = createString("uma resposta entre muitas"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // The translation context provides a bridge between Portuguese & English.
    assertThat(matches).isTrue()
  }

  @Test
  fun testString_inputsAndAnswerInPortuguese_answerNotContained_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = createString("de outros"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // The Portuguese answer doesn't match.
    assertThat(matches).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputContainsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      LocaleProdModule::class, FakeOppiaClockModule::class, LoggerModule::class,
      TestDispatcherModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, AssetModule::class, RobolectricModule::class,
      TestModule::class, PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TextInputContainsRuleClassifierProviderTest)
  }
}
