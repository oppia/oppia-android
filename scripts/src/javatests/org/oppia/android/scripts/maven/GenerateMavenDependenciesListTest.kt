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
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.proto.ExtractedCopyLink
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.ScrapableLink
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/** Tests for [GenerateMavenDependenciesList]. */
class GenerateMavenDependenciesListTest {

  private val THIRD_PARTY_PREFIX = "//third_pary:"
  private val DEP_WITH_SCRAPABLE_LICENSE = "androidx.databinding:databinding-adapters:3.4.2"
  private val DEP_WITH_NO_LICENSE = "com.google.protobuf:protobuf-lite:3.0.0"
  private val DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES =
    "com.github.bumptech.glide:annotations:4.11.0"
  private val DEP_WITH_DIRECT_LINK_ONLY_LICENSE = "com.google.firebase:firebase-analytics:17.5.0"
  private val DEP_WITH_INVALID_LINKS = "io.fabric.sdk.android:fabric:1.4.7"

  private val DATA_BINDING_VERSION = "3.4.2"
  private val PROTO_LITE_VERSION = "3.0.0"
  private val GLIDE_ANNOTATIONS_VERSION = "4.11.0"
  private val FIREBASE_ANALYTICS_VERSION = "17.5.0"
  private val IO_FABRIC_VERSION = "1.4.7"

  private val DATA_BINDING_POM = "https://maven.google.com/androidx/databinding/databinding-" +
    "adapters/$DATA_BINDING_VERSION/databinding-adapters-$DATA_BINDING_VERSION.pom"
  private val PROTO_LITE_POM = "https://repo1.maven.org/maven2/com/google/protobuf/protobuf" +
    "-lite/$PROTO_LITE_VERSION/protobuf-lite-$PROTO_LITE_VERSION.pom"
  private val IO_FABRIC_POM = "https://maven.google.com/io/fabric/sdk/android/fabric/" +
    "$IO_FABRIC_VERSION/fabric-$IO_FABRIC_VERSION.pom"
  private val GLIDE_ANNOTATIONS_POM = "https://repo1.maven.org/maven2/com/github/bumptech/glide" +
    "/annotations/$GLIDE_ANNOTATIONS_VERSION/annotations-$GLIDE_ANNOTATIONS_VERSION.pom"
  private val FIREBASE_ANALYTICS_POM = "https://maven.google.com/com/google/firebase/firebase-" +
    "analytics/$FIREBASE_ANALYTICS_VERSION/firebase-analytics-$FIREBASE_ANALYTICS_VERSION.pom"

  private val LICENSE_DETAILS_INCOMPLETE_FAILURE = "Licenses details are not completed"
  private val UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE =
    "License links are invalid or not available for some dependencies"
  private val SCRIPT_PASSED_MESSAGE =
    "Script executed succesfully: maven_dependencies.textproto updated successfully."

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }
  private val commandExecutor by lazy { initiazeCommandExecutorWithLongProcessWaitTime() }
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

  @Test
  fun testEmptyPbFile_scriptFailsWithException_writesTextproto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "androidx.databinding:databinding-adapters:3.4.2"
        artifact_version: "3.4.2"
        license {
          license_name: "The Apache License, Version 2.0"
          original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      }
      maven_dependency {
        artifact_name: "com.google.firebase:firebase-analytics:17.5.0"
        artifact_version: "17.5.0"
        license {
          license_name: "Android Software Development Kit License"
          original_link: "https://developer.android.com/studio/terms.html"
        }
      }
      """.trimIndent()
    )
  }

  @Test
  fun testLicenseLinkNotVerified_forAtleastOneLicense_scriptFailsWithException() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder()
        .setUrl("https://local-copy/bsd-license").build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addAllLicense(listOf(license1))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
  }

  @Test
  fun testDependencyHasNonScrapableLink_scriptFailsWithException_writesTextproto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "com.google.firebase:firebase-analytics:17.5.0"
        artifact_version: "17.5.0"
        license {
          license_name: "Android Software Development Kit License"
          original_link: "https://developer.android.com/studio/terms.html"
        }
      }
      """.trimIndent()
    )
  }

  @Test
  fun testDependencyHasLocalCopyLinkAndScrapbaleLink_scriptFails_andWritesTextproto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "com.github.bumptech.glide:annotations:4.11.0"
        artifact_version: "4.11.0"
        license {
          license_name: "Simplified BSD License"
          original_link: "https://www.opensource.org/licenses/bsd-license"
        }
        license {
          license_name: "The Apache Software License, Version 2.0"
          original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      }
      """.trimIndent()
    )
  }

  @Test
  fun testDependencyHasInvalidLicense_scriptFailsWithException_writesTextProto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val license1 = License.newBuilder().apply {
      this.licenseName = "Fabric Software and Services Agreement"
      this.originalLink = "https://fabric.io/terms"
      this.isOriginalLinkInvalid = true
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_INVALID_LINKS
            this.artifactVersion = IO_FABRIC_VERSION
            this.addAllLicense(listOf(license1))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(DEP_WITH_INVALID_LINKS)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "io.fabric.sdk.android:fabric:1.4.7"
        artifact_version: "1.4.7"
        license {
          license_name: "Fabric Software and Services Agreement"
          original_link: "https://fabric.io/terms"
          is_original_link_invalid: true
        }
      }
      """.trimIndent()
    )
  }

  @Test
  fun testDependencyHasNoLicense_scriptFails_writesProto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_NO_LICENSE)
    setupBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(
        mockLicenseFetcher,
        commandExecutor
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "com.google.protobuf:protobuf-lite:3.0.0"
        artifact_version: "3.0.0"
      }
      """.trimIndent()
    )
  }

  @Test
  fun testDependenciesHaveMultipleLicense_licenseDetailsCompleted_scriptPasses() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder()
        .setUrl("https://www.apache.org/licenses/LICENSE-2.0.txt").build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder()
        .setUrl("https://local-copy/bsd-license").build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addAllLicense(listOf(license1))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList =
      listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES)
    setupBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(
      mockLicenseFetcher,
      commandExecutor
    ).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)
  }

  @Test
  fun testDependenciesHaveCompleteLicenseDetails_scriptPasses_writesTextProto() {
    val textprotoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder()
        .setUrl("https://www.apache.org/licenses/LICENSE-2.0.txt").build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder()
        .setUrl("https://local-copy/bsd-license").build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addAllLicense(listOf(license1))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_NO_LICENSE
            this.artifactVersion = PROTO_LITE_VERSION
            this.addAllLicense(listOf(license2))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_NO_LICENSE)
    setupBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(
      mockLicenseFetcher,
      commandExecutor
    ).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    val textprotoContent = textprotoFile.readAsJoinedString()
    assertThat(textprotoContent).contains(
      """
      maven_dependency {
        artifact_name: "androidx.databinding:databinding-adapters:3.4.2"
        artifact_version: "3.4.2"
        license {
          license_name: "The Apache License, Version 2.0"
          original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
          scrapable_link {
            url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
      }
      maven_dependency {
        artifact_name: "com.google.protobuf:protobuf-lite:3.0.0"
        artifact_version: "3.0.0"
        license {
          license_name: "Simplified BSD License"
          extracted_copy_link {
            url: "https://local-copy/bsd-license"
          }
        }
      }
      """.trimIndent()
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)
  }

  @Test
  fun testDependencyHasScrapableLicense_scriptPassesAndWriteTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder()
        .setUrl("https://www.apache.org/licenses/LICENSE-2.0.txt").build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addAllLicense(listOf(license1))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE)
    setupBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(
      mockLicenseFetcher,
      commandExecutor
    ).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    val textprotoContent = textProtoFile.readAsJoinedString()
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)
    assertThat(textprotoContent).matches(
      """
      maven_dependency {
        artifact_name: "androidx.databinding:databinding-adapters:3.4.2"
        artifact_version: "3.4.2"
        license {
          license_name: "The Apache License, Version 2.0"
          original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
          scrapable_link {
            url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
      }
      """.trimIndent()
    )
  }

  private fun setupBazelEnvironment(coordsList: List<String>) {
    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
    writeMavenInstallJson(mavenInstallJson)
    testBazelWorkspace.setupWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      "//third_party:${omitVersionAndReplaceColonsHyphensPeriods(coordinate)}"
    }
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList)
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

  private fun createThirdPartyAndroidBinary(
    dependenciesList: List<String>
  ) {
    tempFolder.newFile("AndroidManifest.xml")
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
          manifest = "AndroidManifest.xml",
          deps = depsList
      )
      """.trimIndent() + "\n"
    )
  }

  private fun writeMavenInstallJson(file: File) {
    file.writeText(
      """
      {
        "dependency_tree": {
          "dependencies": [
            {
              "coord": "androidx.databinding:databinding-adapters:3.4.2",
              "url": "${DATA_BINDING_POM.dropLast(3)}aar"
            },
            {
              "coord": "com.github.bumptech.glide:annotations:4.11.0",
              "url": "${GLIDE_ANNOTATIONS_POM.dropLast(3)}jar"
            },
            {
              "coord": "com.google.firebase:firebase-analytics:17.5.0",
              "url": "${FIREBASE_ANALYTICS_POM.dropLast(3)}aar"
            },
            {
               "coord": "com.google.protobuf:protobuf-lite:3.0.0",
               "url": "${PROTO_LITE_POM.dropLast(3)}jar"
            },
            {
              "coord": "io.fabric.sdk.android:fabric:1.4.7",
              "url": "${IO_FABRIC_POM.dropLast(3)}aar"
            }
          ]
        }
      }  
      """.trimIndent()
    )
  }

  private fun File.readAsJoinedString(): String = readLines().joinToString(separator = "\n")

  private fun initiazeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES)
  }

  private fun initializeLicenseFetcher(): LicenseFetcher {
    return mock<LicenseFetcher> {
      on { scrapeText(eq(DATA_BINDING_POM)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <licenses>
            <license>
              <name>The Apache License, Version 2.0</name>
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
              <name>Simplified BSD License</name>
              <url>https://www.opensource.org/licenses/bsd-license</url>
              <distribution>repo</distribution>
            </license>
            <license>
              <name>The Apache Software License, Version 2.0</name>
              <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
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
          <project>Random Project</project>
          """.trimIndent()
        )
    }
  }
}
