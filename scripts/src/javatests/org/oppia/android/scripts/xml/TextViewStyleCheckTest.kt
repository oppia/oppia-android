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

  private fun createStylesFile(content: String) {
    val stylesFile = tempFolder.newFile("app/src/main/res/values/styles.xml")
    stylesFile.writeText(content)
  }

  private fun createLayoutFile(content: String) {
    val layoutFile = tempFolder.newFile("app/src/main/res/layout/test_layout.xml")
    layoutFile.writeText(content)
  }

  private fun runScript() {
    main(tempFolder.root.absolutePath)
  }
}
