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
    val validStyle =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <style name="ValidTextViewStyle">
          <item name="android:textAlignment">viewStart</item>
          <item name="android:textDirection">locale</item>
        </style>
      </resources>
      """.trimIndent()

    val invalidLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_marginStart="8dp"/>
      </LinearLayout>
      """.trimIndent()

    createStylesFile(validStyle)
    createLayoutFile(invalidLayout)

    val thrown = kotlin.runCatching { runScript() }.exceptionOrNull()
    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains("TEXTVIEW STYLE CHECK FAILED")
    assertThat(outContent.toString()).contains("ERROR: Missing style attribute")
    assertThat(outContent.toString()).contains("line 5")
  }

  @Test
  fun testTextViewStyle_nonExistentStyle_checksFail() {
    val validStyle =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <style name="ValidTextViewStyle">
          <item name="android:textAlignment">viewStart</item>
          <item name="android:textDirection">locale</item>
        </style>
      </resources>
      """.trimIndent()

    val invalidLayout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_marginStart="8dp"
          style="@style/NonExistentStyle"/>
      </LinearLayout>
      """.trimIndent()

    createStylesFile(validStyle)
    createLayoutFile(invalidLayout)

    val thrown = kotlin.runCatching { runScript() }.exceptionOrNull()
    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains("TEXTVIEW STYLE CHECK FAILED")
    assertThat(outContent.toString()).contains(
      "ERROR:" +
        " References non-existent style: NonExistentStyle"
    )
    assertThat(outContent.toString()).contains("line 5")
  }

  @Test
  fun testTextViewStyle_styleWithoutRtlProperties_checksFail() {
    val invalidStyle =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <style name="InvalidTextViewStyle">
          <item name="android:layout_width">wrap_content</item>
          <item name="android:layout_height">wrap_content</item>
        </style>
      </resources>
      """.trimIndent()

    val layout =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_marginStart="8dp"
          style="@style/InvalidTextViewStyle"/>
      </LinearLayout>
      """.trimIndent()

    createStylesFile(invalidStyle)
    createLayoutFile(layout)

    val thrown = kotlin.runCatching { runScript() }.exceptionOrNull()
    assertThat(thrown).isInstanceOf(Exception::class.java)
    assertThat(thrown).hasMessageThat().contains("TEXTVIEW STYLE CHECK FAILED")
    assertThat(outContent.toString()).contains(
      "ERROR: Style 'InvalidTextViewStyle'" +
        " lacks RTL/LTR properties in file"
    )
    assertThat(outContent.toString()).contains("line 5")
  }

  @Test
  fun testTextViewStyle_legacyDirectionalityAttributes_showsWarning() {
    val validStyle =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
        <style name="ValidTextViewStyle">
          <item name="android:textAlignment">viewStart</item>
          <item name="android:textDirection">locale</item>
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

    assertThat(outContent.toString()).contains(
      "WARNING: Uses legacy directional attributes: android:paddingLeft, android:layout_marginRight"
    )
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
