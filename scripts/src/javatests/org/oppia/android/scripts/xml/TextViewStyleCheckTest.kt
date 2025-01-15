package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.PrintStream

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
    val validStyle =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <style name="ValidTextViewStyle">
          <item name="android:textAlignment">viewStart</item>
          <item name="android:textDirection">locale</item>
          <item name="android:textSize">16sp</item>
        </style>
      </resources>
      """.trimIndent()

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

    createStylesFile(validStyle)
    createLayoutFile(validLayout)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TEXTVIEW_STYLE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTextViewStyle_missingStyleAttribute_checksFail() {
    val validLayout =
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

    createLayoutFile(validLayout)

    val thrown = catchThrowable { runScript() }

    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")
  }

  @Test
  fun testTextViewStyle_legacyDirectionalityAttributes_showsWarnings() {
    val validStyle =
      """
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
      <style name="ValidTextViewStyle">
        <item name="android:textAlignment">viewStart</item>
        <item name="android:textDirection">locale</item>
        <item name="android:textSize">16sp</item>
      </style>
    </resources>
    """.trimIndent()

    val layoutWithLegacyAttributes =
      """
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
      android:layout_width="match_parent"
      android:layout_height="wrap_content">
      <TextView
        android:id="@+id/warning_test_text_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        style="@style/ValidTextViewStyle"
        android:paddingLeft="16dp"
        android:layout_marginRight="8dp"/>
    </LinearLayout>
    """.trimIndent()

    createStylesFile(validStyle)
    createLayoutFile(layoutWithLegacyAttributes)

    runScript()

    val output = outContent.toString()
    assertThat(output).contains("WARNING: Hardcoded left/right attribute 'android:paddingLeft'")
    assertThat(output).contains("WARNING: Hardcoded left/right attribute 'android:layout_marginRight'")
    assertThat(output).doesNotContain(TEXTVIEW_STYLE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTextViewStyle_multipleTextViews_checksAllTextViews() {
    val layoutWithMultipleTextViews =
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
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(layoutWithMultipleTextViews)

    val thrown = catchThrowable { runScript() }

    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")
  }

  @Test
  fun testTextViewStyle_multipleLayoutFiles_checksAllFiles() {
    val validLayout1 =
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

    val invalidLayout2 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:id="@+id/test_no_style"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"/>
      </LinearLayout>
      """.trimIndent()

    createLayoutFile(validLayout1, "test_layout1.xml")
    createLayoutFile(invalidLayout2, "test_layout2.xml")

    val thrown = catchThrowable { runScript() }

    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains(TEXTVIEW_STYLE_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")
  }

  private fun createStylesFile(content: String) {
    val stylesFile = tempFolder.newFile("app/src/main/res/values/styles.xml")
    stylesFile.writeText(content)
  }

  private fun createLayoutFile(content: String, fileName: String = "test_layout.xml") {
    val layoutFile = tempFolder.newFile("app/src/main/res/layout/$fileName")
    layoutFile.writeText(content)
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }

  private fun catchThrowable(executable: () -> Unit): Throwable {
    try {
      executable()
      throw AssertionError("Expected to throw, but did not")
    } catch (e: Throwable) {
      return e
    }
  }
}
