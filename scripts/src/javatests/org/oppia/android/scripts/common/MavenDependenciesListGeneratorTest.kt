package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.TextFormat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.oppia.android.scripts.proto.DirectLinkOnly
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

/** Tests for [MavenDependenciesListGenerator]. */
class MavenDependenciesListGeneratorTest {

  private val THIRD_PARTY_PREFIX = "//third_pary:"
  private val DEP_WITH_SCRAPABLE_LICENSE = "androidx.databinding:databinding-adapters:3.4.2"
  private val DEP_WITH_NO_LICENSE = "com.google.protobuf:protobuf-lite:3.0.0"
  private val DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES =
    "com.github.bumptech.glide:annotations:4.11.0"
  private val DEP_WITH_DIRECT_LINK_ONLY_LICENSE = "com.google.firebase:firebase-analytics:17.5.0"
  private val DEP_WITH_INVALID_LINKS = "io.fabric.sdk.android:fabric:1.4.7"
  private val DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME =
    "com.squareup.moshi:moshi:1.11.0"

  private val DATA_BINDING_VERSION = "3.4.2"
  private val PROTO_LITE_VERSION = "3.0.0"
  private val GLIDE_ANNOTATIONS_VERSION = "4.11.0"
  private val FIREBASE_ANALYTICS_VERSION = "17.5.0"
  private val IO_FABRIC_VERSION = "1.4.7"
  private val MOSHI_VERSION = "1.11.0"

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
  private val MOSHI_POM = "https://repo1.maven.org/maven2/com/squareup/moshi/moshi/" +
    "$MOSHI_VERSION/moshi-$MOSHI_VERSION.pom"

  private val LICENSE_DETAILS_INCOMPLETE_FAILURE = "Licenses details are not completed"
  private val UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE =
    "License links are invalid or not available for some dependencies"
  private val SCRIPT_PASSED_MESSAGE =
    "Script executed succesfully: maven_dependencies.textproto updated successfully."

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }
  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }
  private val mavenDependenciesListGenerator = MavenDependenciesListGenerator(
    mockLicenseFetcher,
    commandExecutor
  )
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
  fun testRetrieveThirdPartyMavenDepsList_oneDepInDepGraph_returnsCorrectDep() {
    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE)
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      "//third_party:${omitVersionAndReplaceColonsPeriods(coordinate)}"
    }
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList)
    val depsList = mavenDependenciesListGenerator.retrieveThirdPartyMavenDependenciesList(
      "${tempFolder.root}"
    )
    assertThat(depsList).contains(
      omitVersionAndReplaceColonsHyphensPeriods(DEP_WITH_SCRAPABLE_LICENSE)
    )
  }

  @Test
  fun testRetrieveThirdPartyMavenDepsList_multipleDepsInDepGraph_returnsCorrectDeps() {
    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_INVALID_LINKS,
      DEP_WITH_DIRECT_LINK_ONLY_LICENSE
    )
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      "//third_party:${omitVersionAndReplaceColonsPeriods(coordinate)}"
    }
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList)
    val depsList = mavenDependenciesListGenerator.retrieveThirdPartyMavenDependenciesList(
      "${tempFolder.root}"
    )
    assertThat(depsList).contains(
      omitVersionAndReplaceColonsHyphensPeriods(DEP_WITH_SCRAPABLE_LICENSE)
    )
    assertThat(depsList).contains(
      omitVersionAndReplaceColonsHyphensPeriods(DEP_WITH_INVALID_LINKS)
    )
    assertThat(depsList).contains(
      omitVersionAndReplaceColonsHyphensPeriods(
        DEP_WITH_DIRECT_LINK_ONLY_LICENSE
      )
    )
  }

  @Test
  fun testAddChangesFromTextProto_DepsUpdated_returnsDepsListWithUpdatedDeps() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val updatedLicense1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addAllLicense(listOf(license2))
      }.build()
    )
    val updatedMavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(updatedLicense1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addAllLicense(listOf(license2))
      }.build()
    )

    val finalDepsList = mavenDependenciesListGenerator.addChangesFromTextProto(
      mavenDependenciesList,
      updatedMavenDependenciesList
    )
    assertThat(finalDepsList.size).isEqualTo(2)
    assertIsDependency(
      dependency = finalDepsList[0],
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    verifyLicenseHasScrapableVerifiedLink(
      license = finalDepsList[0].licenseList[0],
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    assertIsDependency(
      dependency = finalDepsList[1],
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = finalDepsList[1].licenseList[0],
      licenseName = "Simplified BSD License",
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testAddChangesFromTextProto_bothListsSameInArgs_returnsSameDepsList() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addAllLicense(listOf(license2))
      }.build()
    )
    val updatedMavenDependenciesList = mavenDependenciesList

    val finalDepsList = mavenDependenciesListGenerator.addChangesFromTextProto(
      mavenDependenciesList,
      updatedMavenDependenciesList
    )
    assertThat(finalDepsList.size).isEqualTo(2)
    assertIsDependency(
      dependency = finalDepsList[0],
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    verifyLicenseHasScrapableVerifiedLink(
      license = finalDepsList[0].licenseList[0],
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    assertIsDependency(
      dependency = finalDepsList[1],
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = finalDepsList[1].licenseList[0],
      licenseName = "Simplified BSD License",
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testRetrieveManuallyUpdatedLicensesSet_noLicenseUpdated_returnsEmptySet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addAllLicense(listOf(license2))
      }.build()
    )

    val licenseSet = mavenDependenciesListGenerator.retrieveManuallyUpdatedLicensesSet(
      mavenDependenciesList
    )
    assertThat(licenseSet).isEmpty()
  }

  @Test
  fun testRetrieveManuallyUpdatedLicensesSet_oneLicenseUpdated_returnsCorrectSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addAllLicense(listOf(license2))
      }.build()
    )

    val licenseSet = mavenDependenciesListGenerator.retrieveManuallyUpdatedLicensesSet(
      mavenDependenciesList
    )
    assertThat(licenseSet.size).isEqualTo(1)
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseSet.elementAt(0),
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
  }

  @Test
  fun testRetrieveManuallyUpdatedLicensesSet_multipleLicenseUpdated_returnsCorrectSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val license3 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
      this.directLinkOnly = DirectLinkOnly.newBuilder().apply {
        url = "https://developer.android.com/studio/terms.html"
      }.build()
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE
        this.artifactVersion = FIREBASE_ANALYTICS_VERSION
        this.addLicense(license3)
      }.build()
    )

    val licenseSet = mavenDependenciesListGenerator.retrieveManuallyUpdatedLicensesSet(
      mavenDependenciesList
    )
    assertThat(licenseSet.size).isEqualTo(3)
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseSet.elementAt(0),
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = licenseSet.elementAt(1),
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
    verifyLicenseHasDirectLinkOnlyVerifiedLink(
      license = licenseSet.elementAt(2),
      originalLink = "https://developer.android.com/studio/terms.html",
      licenseName = "Android Software Development Kit License",
      verifiedLink = "https://developer.android.com/studio/terms.html"
    )
  }

  @Test
  fun testUpdateMavenDependenciesList_emptyUpdatedLicenseSet_returnsSameDepsList() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val finalDepsList = mavenDependenciesListGenerator.updateMavenDependenciesList(
      mavenDependenciesList,
      setOf<License>()
    )
    assertThat(finalDepsList).isEqualTo(mavenDependenciesList)
  }

  @Test
  fun testUpdateMavenDependenciesList_nonEmptyUpdatedLicenseSet_returnsCorrectUpdatedDepsList() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val updatedLicense2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val finalDepsList = mavenDependenciesListGenerator.updateMavenDependenciesList(
      mavenDependenciesList,
      setOf<License>(updatedLicense2)
    )
    assertThat(finalDepsList.size).isEqualTo(2)
    assertIsDependency(
      dependency = finalDepsList[0],
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    verifyLicenseHasScrapableVerifiedLink(
      license = finalDepsList[0].licenseList[0],
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    assertIsDependency(
      dependency = finalDepsList[1],
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = finalDepsList[1].licenseList[0],
      licenseName = "Simplified BSD License",
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testWriteTextProto_emptyMavenDependencyList_textProtoFileEmpty() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val mavenDependencyList = MavenDependencyList.newBuilder().build()

    mavenDependenciesListGenerator.writeTextProto(
      "${tempFolder.root}/scripts/assets/maven_dependencies.textproto",
      mavenDependencyList
    )

    assertThat(textProtoFile.readAsJoinedString()).isEqualTo("")
  }

  @Test
  fun testWriteTextProto_nonEmptyMavenDependencyList_writesTextProtoFile() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")

    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE
            this.artifactVersion = FIREBASE_ANALYTICS_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_NO_LICENSE
            this.artifactVersion = PROTO_LITE_VERSION
            this.addLicense(license2)
          }.build()
        )
      )
    }.build()

    mavenDependenciesListGenerator.writeTextProto(
      "${tempFolder.root}/scripts/assets/maven_dependencies.textproto",
      mavenDependencyList
    )

    assertThat(textProtoFile.readAsJoinedString()).isEqualTo(
      """
      maven_dependency {
        artifact_name: "com.google.firebase:firebase-analytics:17.5.0"
        artifact_version: "17.5.0"
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
          original_link: "https://www.opensource.org/licenses/bsd-license"
        }
      }
      """.trimIndent()
    )
  }

  @Test
  fun testGetAllBrokenLicenses_noBrokenLicense_returnsEmptyLicenseSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val brokenLicenses = mavenDependenciesListGenerator.getAllBrokenLicenses(mavenDependenciesList)
    assertThat(brokenLicenses).isEmpty()
  }

  @Test
  fun testGetAllBrokenLicenses_multipleBrokenLicenses_returnsCorrectLicenseSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val brokenLicenses = mavenDependenciesListGenerator.getAllBrokenLicenses(mavenDependenciesList)
    assertThat(brokenLicenses.size).isEqualTo(2)
    verifyLicenseHasVerifiedLinkNotSet(
      license = brokenLicenses.elementAt(0),
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    verifyLicenseHasVerifiedLinkNotSet(
      license = brokenLicenses.elementAt(1),
      licenseName = "Simplified BSD License",
      originalLink = "https://www.opensource.org/licenses/bsd-license"
    )
  }

  @Test
  fun testGetAllBrokenLicenses_licenseOriginalLinkInvalid_notPresentInReturnedSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Crashlytics Terms of Service"
      this.originalLink = "https://try.crashlytics.com/terms/terms-of-service.pdf"
      this.isOriginalLinkInvalid = true
    }.build()
    val mavenDependenciesList = listOf(
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
        this.artifactVersion = DATA_BINDING_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val brokenLicenses = mavenDependenciesListGenerator.getAllBrokenLicenses(mavenDependenciesList)
    assertThat(brokenLicenses).doesNotContain(license2)
  }

  private fun verifyLicenseHasScrapableVerifiedLink(
    license: License,
    originalLink: String,
    licenseName: String,
    verifiedLink: String,
  ) {
    assertThat(license.licenseName).isEqualTo(licenseName)
    assertThat(license.verifiedLinkCase).isEqualTo(
      License.VerifiedLinkCase.SCRAPABLE_LINK
    )
    assertThat(license.scrapableLink.url).isEqualTo(verifiedLink)
    assertThat(license.originalLink).isEqualTo(originalLink)
    assertThat(license.isOriginalLinkInvalid).isFalse()
  }

  private fun verifyLicenseHasExtractedCopyVerifiedLink(
    license: License,
    originalLink: String,
    licenseName: String,
    verifiedLink: String,
  ) {
    assertThat(license.licenseName).isEqualTo(licenseName)
    assertThat(license.verifiedLinkCase).isEqualTo(
      License.VerifiedLinkCase.EXTRACTED_COPY_LINK
    )
    assertThat(license.extractedCopyLink.url).isEqualTo(verifiedLink)
    assertThat(license.originalLink).isEqualTo(originalLink)
    assertThat(license.isOriginalLinkInvalid).isFalse()
  }

  private fun verifyLicenseHasDirectLinkOnlyVerifiedLink(
    license: License,
    originalLink: String,
    licenseName: String,
    verifiedLink: String,
  ) {
    assertThat(license.licenseName).isEqualTo(licenseName)
    assertThat(license.verifiedLinkCase).isEqualTo(
      License.VerifiedLinkCase.DIRECT_LINK_ONLY
    )
    assertThat(license.directLinkOnly.url).isEqualTo(verifiedLink)
    assertThat(license.originalLink).isEqualTo(originalLink)
    assertThat(license.isOriginalLinkInvalid).isFalse()
  }

  private fun verifyLicenseHasVerifiedLinkNotSet(
    license: License,
    originalLink: String,
    licenseName: String
  ) {
    assertThat(license.licenseName).isEqualTo(licenseName)
    assertThat(license.verifiedLinkCase).isEqualTo(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET)
    assertThat(license.originalLink).isEqualTo(originalLink)
    assertThat(license.isOriginalLinkInvalid).isFalse()
  }

  private fun verifyLicenseHasOriginalLinkInvalid(
    license: License,
    originalLink: String,
    licenseName: String
  ) {
    assertThat(license.licenseName).isEqualTo(licenseName)
    assertThat(license.verifiedLinkCase).isEqualTo(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET)
    assertThat(license.originalLink).isEqualTo(originalLink)
    assertThat(license.isOriginalLinkInvalid).isTrue()
  }

  private fun assertIsDependency(
    dependency: MavenDependency,
    artifactName: String,
    artifactVersion: String,
  ) {
    assertThat(dependency.artifactName).isEqualTo(artifactName)
    assertThat(dependency.artifactVersion).isEqualTo(artifactVersion)
  }

  private fun parseTextProto(
    textProtoFile: File,
    proto: MavenDependencyList
  ): MavenDependencyList {
    val builder = proto.newBuilderForType()
    TextFormat.merge(textProtoFile.readText(), builder)
    return builder.build()
  }

  private fun setUpBazelEnvironment(coordsList: List<String>) {
    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
    writeMavenInstallJson(mavenInstallJson)
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      "//third_party:${omitVersionAndReplaceColonsPeriods(coordinate)}"
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
      createThirdPartyAndroidLibrary(thirdPartyBuild, export)
    }
  }

  private fun createThirdPartyAndroidLibrary(thirdPartyBuild: File, artifactName: String) {
    thirdPartyBuild.appendText(
      """
      android_library(
          name = "${omitVersionAndReplaceColonsPeriods(artifactName)}",
          visibility = ["//visibility:public"],
          exports = [artifact("$artifactName")],
      )
      """.trimIndent() + "\n"
    )
  }

  private fun omitVersionAndReplaceColonsPeriods(artifactName: String): String {
    val lastColonIndex = artifactName.lastIndexOf(':')
    return artifactName.substring(0, lastColonIndex).replace('.', '_').replace(':', '_')
  }

  private fun omitVersionAndReplaceColonsHyphensPeriods(artifactName: String): String {
    val lastColonIndex = artifactName.lastIndexOf(':')
    return artifactName.substring(0, lastColonIndex).replace('.', '_').replace(':', '_')
      .replace('-', '_')
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

  /** Helper function to write a fake maven_install.json file. */
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
              "coord": "com.squareup.moshi:moshi:1.11.0",
               "url": "${MOSHI_POM.dropLast(3)}jar"
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

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES)
  }

  /** Returns a mock for the [LicenseFetcher]. */
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
              <name>The Apache License, Version 2.0</name>
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
      on { scrapeText(eq(MOSHI_POM)) }
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
