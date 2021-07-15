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
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

private const val DATA_BINDING_POM = "https://maven.google.com/androidx/databinding/" +
  "databinding-adapters/3.4.2/databinding-adapters-3.4.2.pom"
private const val PROTO_LITE_POM = "https://repo1.maven.org/maven2/com/google/protobuf/" +
  "protobuf-lite/3.0.0/protobuf-lite-3.0.0.pom"
private const val IO_FABRIC_POM = "https://maven.google.com/io/fabric/sdk/android/" +
  "fabric/1.4.7/fabric-1.4.7.pom"
private const val GLIDE_ANNOTATIONS_POM = "https://repo1.maven.org/maven2/com/github/" +
  "bumptech/glide/annotations/4.11.0/annotations-4.11.0.pom"
private const val FIREBASE_ANALYTICS_POM = "https://maven.google.com/com/google/firebase/" +
  "firebase-analytics/17.5.0/firebase-analytics-17.5.0.pom"

/** Tests for [MavenDependenciesListWriter]. */
class MavenDependenciesListWriterTest {

  private val LICENSE_DETAILS_INCOMPLETE_FAILURE = "Licenses details are not completed."
  private val COMPLETE_LICENSE_DETAILS_MESSAGE = "Please complete all the details" +
    "for the following licenses:"

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }
  private lateinit var testBazelWorkspace: TestBazelWorkspace

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFolder("third_party")
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  // 1. Some License requires manual work, script fails.
  // 2. Some Dependency requires manual work, script fails.
  // 3. Some License and dependency both requires manual work, script fails.
  // 4. Textproto is complete, script passes.
  // 5. Some dependency contains empty license list, script fails.
  // 6. Incomplte manual work, script fails.
  // 7. Dependencies contain invalid links, script fails.

  @Test
  fun testLicenseNeedManualWork_scriptFailsWithException() {
//    val dependencies = listOf<DependencyName>(
//      DependencyName.DATA_BINDING,
//      DependencyName.FIREBASE_ANALYTICS
//    )
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
//    val mavenDependencyList = getMavenDependencyList(dependencies)
//    mavenDependencyList.writeTo(pbFile.outputStream())

    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
    testBazelWorkspace.ensureWorkspaceIsConfiguredForRulesJvmExternal(
      listOf(
        "androidx.databinding:databinding-adapters:3.4.2",
        "com.google.firebase:firebase-analytics:17.5.0"
      )
    )
    createAndroidBinary(
      listOf(
        "//third_party:androidx_databinding_databinding-adapters",
        "//third_party:com_google_firebase_firebase-analytics"
      )
    )
    writeThirdPartyBuildFile(
      listOf(
        "androidx.databinding:databinding-adapters:3.4.2",
        "com.google.firebase:firebase-analytics:17.5.0"
      )
    )

    writeMavenInstallJson(mavenInstallJson)
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")

    MavenDependenciesListWriter.licenseFetcher = mockLicenseFetcher

//    MavenDependenciesListWriter.main(
//      arrayOf(
//        "${tempFolder.root}",
//        "scripts/assets/maven_install.json",
//        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
//      )
//    )
//
//    assertThat(outContent.toString()).contains("jvdssdd")

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListWriter.main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
  }
//
//  @Test
//  fun testDependenciesNeedManualWork_scriptFailsWithException() {
//    val dependencies = listOf<DependencyName>(
//      DependencyName.PROTO_LITE
//    )
//    val mavenDependencyList = getMavenDependencyList(dependencies)
//
//    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
//    writeMavenInstallJson(mavenInstallJson)
//    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
//    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
//    mavenDependencyList.writeTo(pbFile.outputStream())
//
//    MavenDependenciesListWriter.networkAndBazelUtils = mockNetworkAndBazelUtils
//    MavenDependenciesListWriter.main(
//      arrayOf(
//        "${tempFolder.root}",
//        "scripts/assets/maven_install.json",
//        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
//      )
//    )
//    assertThat(outContent.toString()).contains(COMPLETE_LICENSE_DETAILS_MESSAGE)
//    val exception = assertThrows(Exception::class) {
//      MavenDependenciesListWriter.main(
//        arrayOf(
//          "${tempFolder.root}",
//          "scripts/assets/maven_install.json",
//          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
//        )
//      )
//    }
//
//    assertThat(exception).hasMessageThat().contains(COMPLETE_LICENSE_DETAILS_MESSAGE)
//  }

  @Test
  fun testMockitoUnitTest() {
    assertThat(mockLicenseFetcher.scrapeText("https://www.google.com"))
      .contains("passed")
  }

  private fun writeThirdPartyBuildFile(exportsList: List<String>) {
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    thirdPartyBuild.appendText(
      """
      load("@rules_jvm_external//:defs.bzl", "artifact")
      """.trimIndent() + "\n"
    )
    for (export in exportsList) {
      createAndroidLibrary(thirdPartyBuild, export)
    }
  }

  private fun createAndroidLibrary(thirdPartyBuild: File, artifactName: String) {
    thirdPartyBuild.appendText(
      """
      android_library(
          name = "${omitVersionAndReplaceColonsHyphensPeriods(artifactName)}",
          visibility = ["//visibility:public"],
          exports = [artifact("$artifactName")],
      )
      """.trimIndent() + "\n"
    )
  }

  private fun omitVersionAndReplaceColonsHyphensPeriods(artifactName: String): String {
    val lastColonIndex = artifactName.lastIndexOf(':')
    return artifactName.substring(0, lastColonIndex).replace('.', '_').replace(':', '_')
  }

  fun createAndroidBinary(
    dependenciesList: List<String>
  ) {
    tempFolder.newFile("test_manifest.xml")
    val build = tempFolder.newFile("BUILD.bazel")
    build.appendText("depsList = [\n")
    for (dep in dependenciesList) {
      build.appendText("\"$dep\",")
    }
    build.appendText("]\n")
    build.appendText(
      """
      android_binary(
          name = "oppia",
          manifest = "test_manifest.xml",
          deps = depsList
      )
      """.trimIndent() + "\n"
    )
  }

  private fun getMavenDependency(
    artifactName: String,
    version: String,
    licenseList: List<License> = listOf<License>()
  ): MavenDependency {
    return MavenDependency
      .newBuilder()
      .setArtifactName(artifactName)
      .setArtifactVersion(version)
      .addAllLicense(licenseList)
      .build()
  }

  private fun getMavenDependencyList(
    dependencyNamesList: List<DependencyName>
  ): MavenDependencyList {
    val dependenciesList = mutableListOf<MavenDependency>()
    dependencyNamesList.forEach { dependencyName ->
      when (dependencyName) {
        DependencyName.FIREBASE_ANALYTICS ->
          dependenciesList.add(
            getMavenDependency(
              artifactName = "com.google.firebase:firebase-analytics:17.5.0",
              version = "17.5.0"
            )
          )
        DependencyName.PROTO_LITE ->
          dependenciesList.add(
            getMavenDependency(
              artifactName = "com.google.protobuf:protobuf-lite:3.0.0",
              version = "3.0.0"
            )
          )
        DependencyName.GLIDE_ANNOTATIONS ->
          dependenciesList.add(
            getMavenDependency(
              artifactName = "com.github.bumptech.glide:annotations:4.11.0",
              version = "4.11.0"
            )
          )
        DependencyName.IO_FABRIC ->
          dependenciesList.add(
            getMavenDependency(
              artifactName = "io.fabric.sdk.android:fabric:1.4.7",
              version = "1.4.7"
            )
          )
        else -> dependenciesList.add(
          getMavenDependency(
            artifactName = "androidx.databinding:databinding-adapters:3.4.2",
            version = "3.4.2"
          )
        )
      }
    }
    return MavenDependencyList
      .newBuilder()
      .addAllMavenDependency(dependenciesList)
      .build()
  }

  private fun getLicense(
    licenseName: String,
    originalLink: String = "",
  ): License {
    return License
      .newBuilder()
      .setLicenseName(licenseName)
      .setOriginalLink(originalLink)
      .build()
  }

  private fun writeMavenInstallJson(file: File) {
    file.writeText(
      """
      {
        "dependency_tree": {
          "dependencies": [
            {
              "coord": "androidx.databinding:databinding-adapters:3.4.2",
              "url": "https://maven.google.com/androidx/databinding/databinding-adapters/3.4.2/databinding-adapters-3.4.2.aar"
            },
            {
              "coord": "com.github.bumptech.glide:annotations:4.11.0",
              "url": "https://repo1.maven.org/maven2/com/github/bumptech/glide/annotations/4.11.0/annotations-4.11.0.jar"
            },
            {
              "coord": "com.google.firebase:firebase-analytics:17.5.0",
              "url": "https://maven.google.com/com/google/firebase/firebase-analytics/17.5.0/firebase-analytics-17.5.0.aar"
            },
            {
                "coord": "com.google.protobuf:protobuf-lite:3.0.0",
                "url": "https://repo1.maven.org/maven2/com/google/protobuf/protobuf-lite/3.0.0/protobuf-lite-3.0.0.jar"
            },
            {
              "coord": "io.fabric.sdk.android:fabric:1.4.7",
              "url": "https://maven.google.com/io/fabric/sdk/android/fabric/1.4.7/fabric-1.4.7.aar"
            }
          ]
        }
      }  
      """.trimIndent()
    )
  }

  private enum class DependencyLicenseType {
    NO_LICENSES,
    PLAIN_TEXT_AND_SCRAPABLE_LICENSE,
    PLAIN_TEXT_AND_NONSCRAPABLE_LICENSE,
    NON_PLAIN_TEXT_LICENSE,
    INVALID_LINK_LICENSE;
  }

  private enum class DependencyName {
    GLIDE_ANNOTATIONS,
    FIREBASE_ANALYTICS,
    DATA_BINDING,
    PROTO_LITE,
    IO_FABRIC;
  }

  private fun initializeLicenseFetcher(): LicenseFetcher {
    return mock<LicenseFetcher> {
      on { scrapeText(eq("https://www.google.com")) } doReturn "passed"
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
      on { scrapeText(eq(FIREBASE_ANALYTICS_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <licenses>
            <license>
              <name>Android Software Development Kit License</name>
              <url>https://developer.android.com/studio/terms.html</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          """.trimIndent()
        )
      on { scrapeText(eq(IO_FABRIC_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <licenses>
            <license>
              <name>Fabric Terms of Service</name>
              <url>https://www.fabric.io.terms</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          """.trimIndent()
        )
      on { scrapeText(eq(PROTO_LITE_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          """.trimIndent()
        )
    }
  }
}
