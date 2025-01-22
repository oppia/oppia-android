package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.IllegalStateException

/** Tests for [TextViewStyleCheck]. */
class TextViewStyleCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val TEXTVIEW_STYLE_CHECK_PASSED_OUTPUT_INDICATOR = "TEXTVIEW STYLE CHECK PASSED"
  private val TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR = "TEXTVIEW STYLE CHECK FAILED"

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("app", "src", "main", "res")
    tempFolder.newFolder("app/src/main/res/layout")
    tempFolder.newFolder("app/src/main/res/values")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testTextViewStyle_validStyleAttribute_checksPass() {
    val validLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(validLayout)
    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TEXTVIEW_STYLE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTextViewStyle_missingStyleAttribute_checksFail() {
    val invalidLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:id="@+id/test_text_view_no_style"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(invalidLayout)

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)

    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")
  }

  @Test
  fun testTextViewStyle_onlyDirectionalityWarnings_checksPass() {
    val layoutWithDirectionalityWarnings =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"
          android:paddingLeft="16dp"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithDirectionalityWarnings)
    runScript()

    val output = outContent.toString()
    assertThat(output).contains("WARNING: Hardcoded left/right attribute")
  }

  @Test
  fun testTextViewStyle_multipleTextViews_someWithoutStyle_checksFail() {
    val layoutWithMixedTextViews =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:id="@+id/first_text_view"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"/>
        <TextView
          android:id="@+id/second_text_view_no_style"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
        <TextView
          android:id="@+id/third_text_view"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/AnotherValidStyle"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithMixedTextViews)

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")

    val output = outContent.toString()
    assertThat(output).contains("@+id/second_text_view_no_style")
    assertThat(output).contains("line 10")
  }

  @Test
  fun testTextViewStyle_multipleLayoutFiles_mixedValidation_checksFail() {
    val validLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"/>
      </LinearLayout>
      """.trimIndent()

    val warningLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"
          android:paddingLeft="16dp"/>
      </LinearLayout>
      """.trimIndent()

    val invalidLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(validLayout, "valid_layout.xml")
    createLayoutFile(warningLayout, "warning_layout.xml")
    createLayoutFile(invalidLayout, "invalid_layout.xml")

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)

    val output = outContent.toString()
    assertThat(output).contains("WARNING: Hardcoded left/right attribute")
    assertThat(output).contains("ERROR: Missing style attribute")
  }

  @Test
  fun testTextViewStyle_missingStyle_logsCorrectLineNumber() {
    val layoutWithLineSpacing =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <!-- Comment to affect line numbers -->
        <TextView
          android:id="@+id/test_text_view_no_style"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithLineSpacing)

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString()).contains("line 7")
  }

  @Test
  fun testTextViewStyle_directionalityWarning_logsCorrectLineNumber() {
    val layoutWithLineSpacing =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <!-- First comment -->
        
        <!-- Second comment -->
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"
          android:paddingLeft="16dp"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithLineSpacing)
    runScript()

    assertThat(outContent.toString()).contains("line 9")
  }

  @Test
  fun testTextViewStyle_multipleTextViews_logsCorrectLineNumbers() {
    val layoutWithMultipleTextViews =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <TextView
          android:id="@+id/first_text_view"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
          
        <!-- Spacing comment -->
        
        <TextView
          android:id="@+id/second_text_view"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"
          android:paddingRight="16dp"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithMultipleTextViews)

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)

    val output = outContent.toString()
    assertThat(output).contains("@+id/first_text_view")
    assertThat(output).contains("line 6")
    assertThat(output).contains("line 13")
  }

  @Test
  fun testTextViewStyle_nestedTextViews_logsCorrectLineNumbers() {
    val nestedLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <LinearLayout
          android:layout_width="wrap_content"
          android:layout_height="wrap_content">
          
          <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
            
        </LinearLayout>
        
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          style="@style/ValidTextViewStyle"
          android:layout_marginRight="8dp"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(nestedLayout)

    val exception = assertThrows<IllegalStateException>() { runScript() }
    assertThat(exception).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)

    val output = outContent.toString()
    assertThat(output).contains("line 10")
    assertThat(output).contains("line 16")
  }

  private fun createLayoutFile(content: String, fileName: String = "test_layout.xml") {
    val layoutFile = tempFolder.newFile("app/src/main/res/layout/$fileName")
    layoutFile.writeText(content)
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }
}
