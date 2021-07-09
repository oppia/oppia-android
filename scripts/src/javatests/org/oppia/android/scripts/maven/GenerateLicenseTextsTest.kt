package org.oppia.android.scripts.maven

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.PrimaryLinkType
import org.oppia.android.testing.assertThrows
import java.io.BufferedReader
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [GenerateLicenseTexts]. */
class GenerateLicenseTextsTest {
  private val TOO_LESS_ARGS_FAILURE = "Too less Arguments"
  private val MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE =
    "maven_dependencies.textproto is not up-to-date"
  private val SCRIPT_PASSED_INDICATOR = "Script execution completed."
  private val VALID_LINK = "https://www.apache.org/licenses/LICENSE-2.0.txt"
  private val INVALID_LINK = "https://fabric.io/terms"
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("values")
    tempFolder.newFolder("assets")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testScript_noArguments_printsUsageStringAndThrowsException() {
    val exception = assertThrows(Exception::class) { main(arrayOf()) }

    assertThat(exception).hasMessageThat().contains("Too less arguments passed.")
    assertThat(outContent.toString().trim()).contains("Usage:")
  }

  @Test
  fun testScript_dependencyListEmpty_failsWithNotUpToDateException() {
    val mavenDependencyList = getMavenDependencyList()

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) { runScript() }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_licenseListEmpty_failsWithNotUpToDateException() {
    val dependencyList = listOf<MavenDependency>(getMavenDependency("artifact:name"))
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) { runScript() }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_primaryLinkTypeUnspecified_failsWithNotUpToDateException() {
    val licenseList = listOf<License>(
      getLicense(
        licenseName = "license_0",
        primaryLink = VALID_LINK
      )
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) { runScript() }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_primaryLinkTypeNeedsIntervention_failsWithNotUpToDateException() {
    val licenseList = listOf<License>(
      getLicense(
        licenseName = "license_0",
        primaryLink = INVALID_LINK,
        primaryLinkType = PrimaryLinkType.NEEDS_INTERVENTION
      )
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) { runScript() }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_validDependencyList_passes() {
    val licenseList = listOf<License>(
      getLicense(
        licenseName = "license_0",
        primaryLink = VALID_LINK,
        primaryLinkType = PrimaryLinkType.SCRAPE_DIRECTLY
      )
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    runScript()

    assertThat(outContent.toString().trim()).contains(SCRIPT_PASSED_INDICATOR)
  }

  @Test
  fun testScript_validDependencyList_generatesResourceXmlFiles() {
    val licenseList = listOf<License>(
      getLicense(
        licenseName = "license_0",
        primaryLink = VALID_LINK,
        primaryLinkType = PrimaryLinkType.SCRAPE_DIRECTLY
      )
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    runScript()

    assertXmlGenerated("test_third_party_dependency_names.xml")
    assertXmlGenerated("test_third_party_dependenct_versions.xml")
    assertXmlGenerated("test_third_party_dependency_license_texts.xml")
    assertXmlGenerated("test_third_party_dependency_license_texts_array.xml")
    assertXmlGenerated("test_third_party_dependency_license_names_array.xml")
  }

  private fun assertXmlGenerated(filename: String) {
    val text = File("${tempFolder.root}/values/$filename").bufferedReader().use {
      it.readText()
    }
    val xmlHeader =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      """.trimIndent()
    assertThat(text).contains(xmlHeader)
  }

  private fun getMavenDependency(
    artifactName: String,
    licenseList: List<License> = listOf<License>()
  ): MavenDependency {
    return MavenDependency
      .newBuilder()
      .setArtifactName(artifactName)
      .setArtifactVersion("1.0")
      .addAllLicense(licenseList)
      .build()
  }

  private fun getMavenDependencyList(
    dependenciesList: List<MavenDependency> = listOf<MavenDependency>()
  ): MavenDependencyList {
    return MavenDependencyList
      .newBuilder()
      .addAllMavenDependency(dependenciesList)
      .build()
  }

  private fun getLicense(
    licenseName: String,
    primaryLink: String = "",
    primaryLinkType: PrimaryLinkType = PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED,
    alternativeLink: String = ""
  ): License {
    return License
      .newBuilder()
      .setLicenseName(licenseName)
      .setPrimaryLink(primaryLink)
      .setPrimaryLinkType(primaryLinkType)
      .setAlternativeLink(alternativeLink)
      .build()
  }

  /** Runs the script GenerateLicenseText.kt. */
  private fun runScript() {
    main(
      arrayOf<String>(
        "${tempFolder.root}/values",
        "test_third_party_dependency_names.xml",
        "test_third_party_dependenct_versions.xml",
        "test_third_party_dependency_license_texts.xml",
        "test_third_party_dependency_license_texts_array.xml",
        "test_third_party_dependency_license_names_array.xml",
        "${tempFolder.root}/assets/test_maven_dependencies.pb"
      )
    )
  }
}
