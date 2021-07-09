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
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [GenerateLicenseTexts]. */
class GenerateLicenseTextsTest {
  private val TOO_LESS_ARGS_FAILURE = "Too less Arguments"
  private val MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE =
    "maven_dependencies.textproto is not up-to-date"
  private val SCRIPT_PASSED_INDICATOR = "Script execution completed."
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

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
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
    primaryLinkType: PrimaryLinkType = PrimaryLinkType.PRIMARY_LINK_UNSPECIFIED,
    alternativeLink: String = ""
  ) {
    return License
      .newBuilder()
      .setLicenseName(licenseName)
      .setPrimaryLink(primaryLink)
      .setPrimaryLinkType(primaryLinkType)
      .setAlternativeLink(alternativeLink)
      .build()
  }

  /** Runs the generate_. */
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
