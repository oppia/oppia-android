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

/** Tests for [TextInputFuzzyEqualsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputFuzzyEqualsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_ANSWER_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_ANSWER_LOWERCASE = createString(value = "test")
  private val STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE = createString(value = "diff")
  private val STRING_VALUE_TEST_DIFF_UPPERCASE = createString(value = "DIFF")
  private val STRING_VALUE_TEST_ANSWER = createString(value = "this is a test")
  private val STRING_VALUE_TEST_WITH_WHITESPACE = createString(value = "  test   ")
  private val STRING_VALUE_THIS = createString(value = "this")
  private val STRING_VALUE_TEST = createString(value = "test")
  private val STRING_VALUE_TESTING = createString(value = "testing")
  private val NON_NEGATIVE_TEST_VALUE_1 = createNonNegativeInt(value = 1)

  private val TEST_STRING_CONTENT_ID = "test_content_id"
  private val STRING_VALUE_TEST_AN_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("an answer", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_DIFF_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("diff", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_FUZZY_INPUT_INPUT_SET =
    createTranslatableSetOfNormalizedString("This Is a TesT", contentId = TEST_STRING_CONTENT_ID)
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString(
      "Thiss", "Iis", "TesTt", contentId = TEST_STRING_CONTENT_ID
    )

  @Inject
  internal lateinit var textInputFuzzyEqualsRuleClassifierProvider:
    TextInputFuzzyEqualsRuleClassifierProvider

  private val inputFuzzyEqualsRuleClassifier by lazy {
    textInputFuzzyEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testUpperCaseAnswer_testUpperCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowerCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowercaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_DIFF_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUpperCaseAnswer_testUpperCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_DIFF_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_LOWERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_LOWERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringFuzzyInput_answerFuzzyEqualsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_FUZZY_INPUT_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringWithWhitespacesInput_answerEqualsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_WITH_WHITESPACE,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsFuzzilyToOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_THIS,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsFuzzilyToAnotherOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToNone_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TESTING,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_verifyThrowsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_TEST_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
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
  fun testStringAnswer_inputsWithPortuguese_answerInEnglish_englishContext_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("an answer"),
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsWithPortuguese_answerInPortuguese_englishContext_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("uma resposta"),
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    // A Portuguese answer isn't reocgnized with this translation context.
    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_inputsWithPortuguese_answerInEnglish_portugueseContext_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("an answer"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // Even though the English string matches, the presence of the Portuguese context should trigger
    // a failure for an English answer.
    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_inputsWithPortuguese_answerInPortuguese_portugueseContext_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("uma resposta"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // The translation context provides a bridge between Portuguese & English.
    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsAndAnswerInPortuguese_slightlyMisspelled_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("uma reposta"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // A single misspelled letter should still result in a match in the same way as English.
    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsAndAnswerInPortuguese_largelyMisspelled_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("reposta"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // A missing word & a misspelled word should result in no match.
    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_inputsAndAnswerInArabic_slightlyMisspelled_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("إجاب"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "إجابة")
    )

    // A single misspelled letter should still result in a match in the same way as English.
    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsAndAnswerInArabic_largelyMisspelled_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = createString("إجا"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // Multiple missing letters should result in no match.
    assertThat(matches).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputFuzzyEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
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

  @Singleton
  @Component(
    modules = [
      LocaleProdModule::class, FakeOppiaClockModule::class, LoggerModule::class,
      TestDispatcherModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, AssetModule::class, RobolectricModule::class,
      TestModule::class, PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TextInputFuzzyEqualsRuleClassifierProviderTest)
  }
}
