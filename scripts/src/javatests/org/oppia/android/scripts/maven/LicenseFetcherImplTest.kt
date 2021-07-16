package org.oppia.android.scripts.maven

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [LicenseFetcherImpl]. */
class LicenseFetcherImplTest {

  private val DATA_BINDING_POM = "https://maven.google.com/androidx/databinding/" +
    "databinding-adapters/3.4.2/databinding-adapters-3.4.2.pom"
  private val PROTO_LITE_POM = "https://repo1.maven.org/maven2/com/google/protobuf/" +
    "protobuf-lite/3.0.0/protobuf-lite-3.0.0.pom"
  private val IO_FABRIC_POM = "https://maven.google.com/io/fabric/sdk/android/" +
    "fabric/1.4.7/fabric-1.4.7.pom"
  private val GLIDE_ANNOTATIONS_POM = "https://repo1.maven.org/maven2/com/github/" +
    "bumptech/glide/annotations/4.11.0/annotations-4.11.0.pom"
  private val FIREBASE_ANALYTICS_POM = "https://maven.google.com/com/google/firebase/" +
    "firebase-analytics/17.5.0/firebase-analytics-17.5.0.pom"

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testScrapeText_DataBindingPomLink_returnsDataBindingPom() {
    val pomText = mockLicenseFetcher.scrapeText(DATA_BINDING_POM)
    assertThat(pomText).isEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <licenses>
        <license>
          <name>The Apache Software License, Version 2.0</name>
          <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      """.trimIndent()
    )
  }

  @Test
  fun testScrapeText_GlideAnnotationsPomLink_returnsGlideAnnotationsPom() {
    val pomText = mockLicenseFetcher.scrapeText(GLIDE_ANNOTATIONS_POM)
    assertThat(pomText).isEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <licenses>
        <license>
          <name>The MIT License</name>
          <url>https://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      """.trimIndent()
    )
  }

  @Test
  fun testScrapeText_ProtoLitePomLink_returnsProtoLitePom() {
    val pomText = mockLicenseFetcher.scrapeText(PROTO_LITE_POM)
    assertThat(pomText).isEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project>Random Project</project>
      """.trimIndent()
    )
  }

  @Test
  fun testScrapeText_ProtoLitePomLink_doesNotReturnGlideAnnotationsPom() {
    val pomText = mockLicenseFetcher.scrapeText(PROTO_LITE_POM)
    assertThat(pomText).isNotEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <licenses>
        <license>
          <name>The MIT License</name>
          <url>https://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      """.trimIndent()
    )
  }

  @Test
  fun testScrapeText_DataBindingPomLink_doesNotReturnProtoLitePom() {
    val pomText = mockLicenseFetcher.scrapeText(DATA_BINDING_POM)
    assertThat(pomText).isNotEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project>Random Project</project>
      """.trimIndent()
    )
  }

  @Test
  fun testScrapeText_GlideAnnotationsPomLink_doesNotReturnDataBindingPom() {
    val pomText = mockLicenseFetcher.scrapeText(GLIDE_ANNOTATIONS_POM)
    assertThat(pomText).isNotEqualTo(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <licenses>
        <license>
          <name>The Apache Software License, Version 2.0</name>
          <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      """.trimIndent()
    )
  }

  private fun initializeLicenseFetcher(): LicenseFetcher {
    return mock<LicenseFetcher> {
      on { scrapeText(eq(DATA_BINDING_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <licenses>
            <license>
              <name>The Apache Software License, Version 2.0</name>
              <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          """.trimIndent()
        )
      on { scrapeText(eq(GLIDE_ANNOTATIONS_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <licenses>
            <license>
              <name>The MIT License</name>
              <url>https://opensource.org/licenses/MIT</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          """.trimIndent()
        )
      on { scrapeText(eq(PROTO_LITE_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <project>Random Project</project>
          """.trimIndent()
        )
    }
  }
}
