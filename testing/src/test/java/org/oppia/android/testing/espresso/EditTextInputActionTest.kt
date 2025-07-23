package org.oppia.android.testing.espresso

import android.app.Application
import android.content.Context
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [EditTextInputAction] to verify correct append and replace text behavior
 * in Robolectric environments.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = EditTextInputActionTest.TestApplication::class)
class EditTextInputActionTest {

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var editText: EditText

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    editText = EditText(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun testAppendText_withEmptyText_setsTextCorrectly() {
    editText.setText("")
    
    editTextInputAction.appendText("Hello").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Hello")
  }

  @Test
  fun testAppendText_withExistingText_appendsCorrectly() {
    editText.setText("123")
    
    editTextInputAction.appendText("45").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("12345")
  }

  @Test
  fun testAppendText_multipleAppends_appendsCorrectly() {
    editText.setText("A")
    
    editTextInputAction.appendText("B").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    editTextInputAction.appendText("C").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("ABC")
  }

  @Test
  fun testAppendText_withSpaces_appendsCorrectly() {
    editText.setText("Hello")
    
    editTextInputAction.appendText(" World").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Hello World")
  }

  @Test
  fun testReplaceText_withEmptyText_setsTextCorrectly() {
    editText.setText("")
    
    editTextInputAction.replaceText("Hello").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Hello")
  }

  @Test
  fun testReplaceText_withExistingText_replacesCorrectly() {
    editText.setText("123")
    
    editTextInputAction.replaceText("45").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("45")
  }

  @Test
  fun testReplaceText_multipleReplaces_replacesCorrectly() {
    editText.setText("Original")
    
    editTextInputAction.replaceText("First").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    editTextInputAction.replaceText("Second").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Second")
  }

  @Test
  fun testAppendText_afterReplaceText_behavesCorrectly() {
    editText.setText("Original")
    
    editTextInputAction.replaceText("New").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    editTextInputAction.appendText("Text").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("NewText")
  }

  @Test
  fun testReplaceText_afterAppendText_behavesCorrectly() {
    editText.setText("Base")
    
    editTextInputAction.appendText("Added").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    editTextInputAction.replaceText("Replaced").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Replaced")
  }

  @Test
  fun testAppendText_withSpecialCharacters_appendsCorrectly() {
    editText.setText("Test")
    
    editTextInputAction.appendText("@#$%").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Test@#$%")
  }

  @Test
  fun testReplaceText_withEmptyString_clearsText() {
    editText.setText("SomeText")
    
    editTextInputAction.replaceText("").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("")
  }

  @Test
  fun testAppendText_withEmptyString_doesNotChangeText() {
    editText.setText("Original")
    
    editTextInputAction.appendText("").perform(null, editText)
    testCoroutineDispatchers.runCurrent()
    
    assertThat(editText.text.toString()).isEqualTo("Original")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
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
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(editTextInputActionTest: EditTextInputActionTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEditTextInputActionTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(editTextInputActionTest: EditTextInputActionTest) {
      component.inject(editTextInputActionTest)
    }
  }
}
