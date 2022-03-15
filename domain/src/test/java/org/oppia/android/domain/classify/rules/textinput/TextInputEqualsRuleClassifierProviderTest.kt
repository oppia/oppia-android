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
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TextInputEqualsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputEqualsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_LOWERCASE = createString(value = "test")
  private val STRING_VALUE_TEST_DIFFERENT_VALUE = createString(value = "string")
  private val STRING_VALUE_TEST_SINGLE_SPACES = createString(value = "test a lot")
  private val STRING_VALUE_THIS = createString(value = "this")
  private val STRING_VALUE_A_TEST = createString(value = "a test")
  private val STRING_VALUE_NOT_A_TEST = createString(value = "not a test")
  private val INT_VALUE_TEST_NON_NEGATIVE = createNonNegativeInt(value = 1)

  private val TEST_STRING_CONTENT_ID = "test_content_id"
  private val STRING_VALUE_TEST_AN_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("an answer", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_EXTRA_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("test  a  lot  ", contentId = TEST_STRING_CONTENT_ID)
  private val STRING_VALUE_TEST_NO_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("testalot", contentId = TEST_STRING_CONTENT_ID)
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString(
      "this", "is", "a test", contentId = TEST_STRING_CONTENT_ID
    )

  @Inject
  internal lateinit var textInputEqualsRuleClassifierProvider:
    TextInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    textInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameExactString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testCapitalStringAnswer_lowercaseStringInput_sameCaseInsensitiveString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_UPPERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAnswer_capitalStringInput_sameCaseInsensitiveString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_LOWERCASE,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_sameStringDifferentSpaces_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACES_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_SINGLE_SPACES,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_differentStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_DIFFERENT_VALUE,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_differentStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACES_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_SINGLE_SPACES,
        inputs = inputs,
        writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_THIS,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToDifferentOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_A_TEST,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerDoesNotCorrespondsToAny_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_NOT_A_TEST,
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_LOWERCASE,
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
    val inputs = mapOf("x" to INT_VALUE_TEST_NON_NEGATIVE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_UPPERCASE,
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

    val matches = inputEqualsRuleClassifier.matches(
      answer = createString("an answer"),
      inputs = inputs,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsWithPortuguese_answerInPortuguese_englishContext_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
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

    val matches = inputEqualsRuleClassifier.matches(
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

    val matches = inputEqualsRuleClassifier.matches(
      answer = createString("uma resposta"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // The translation context provides a bridge between Portuguese & English.
    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_inputsAndAnswerInPortuguese_differentAnswer_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = createString("uma resposta diferente"),
      inputs = inputs,
      writtenTranslationContext = createTranslationContext(TEST_STRING_CONTENT_ID, "uma resposta")
    )

    // The Portuguese answer doesn't match.
    assertThat(matches).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
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
      TestModule::class, SyncStatusModule::class, PlatformParameterModule::class,
      LoggingIdentifierModule::class, PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TextInputEqualsRuleClassifierProviderTest)
  }
}
