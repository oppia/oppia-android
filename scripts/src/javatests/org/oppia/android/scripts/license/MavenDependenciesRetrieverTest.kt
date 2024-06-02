package org.oppia.android.scripts.license

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.TextFormat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.license.MavenDependenciesRetriever.MavenListDependency
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

/** Tests for [MavenDependenciesRetriever]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
class MavenDependenciesRetrieverTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockArtifactPropertyFetcher by lazy { initializeArtifactPropertyFetcher() }
  private val commandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }
  private val retriever by lazy { initializeMavenDependenciesRetriever() }
  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

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
    scriptBgDispatcher.close()
  }

  @Test
  fun testRetrieveThirdPartyMavenDepsList_oneDepInDepGraph_returnsCorrectDep() {
    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE)
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = listOf(DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX)
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList, thirdPartyPrefixCoordList)

    val depsList = retriever.retrieveThirdPartyMavenDependenciesList()

    assertThat(depsList).contains(DATA_BINDING_DEP)
  }

  @Test
  fun testRetrieveThirdPartyMavenDepsList_multipleDepsInDepGraph_returnsCorrectDeps() {
    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_INVALID_LINKS,
      DEP_WITH_DIRECT_LINK_ONLY_LICENSE
    )
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = listOf(
      DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX,
      IO_FABRIC_DEP_WITH_THIRD_PARTY_PREFIX,
      FIREBASE_DEP_WITH_THIRD_PARTY_PREFIX
    )
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList, thirdPartyPrefixCoordList)

    val depsList = retriever.retrieveThirdPartyMavenDependenciesList()

    assertThat(depsList).contains(DATA_BINDING_DEP)
    assertThat(depsList).contains(IO_FABRIC_DEP)
    assertThat(depsList).contains(FIREBASE_DEP)
  }

  @Test
  fun testAddChangesFromTextProto_depsUpdated_returnsDepsListWithUpdatedDeps() {
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

    val finalDepsList = retriever.addChangesFromTextProto(
      mavenDependenciesList,
      updatedMavenDependenciesList
    )
    assertThat(finalDepsList).hasSize(2)
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

    val finalDepsList = retriever.addChangesFromTextProto(
      dependencyListFromPom = mavenDependenciesList,
      dependencyListFromProto = mavenDependenciesList
    )
    assertThat(finalDepsList).hasSize(2)
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

    val licenseSet = retriever.retrieveManuallyUpdatedLicensesSet(mavenDependenciesList)
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

    val licenseSet = retriever.retrieveManuallyUpdatedLicensesSet(mavenDependenciesList)
    assertThat(licenseSet).hasSize(1)
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

    val licenseSet = retriever.retrieveManuallyUpdatedLicensesSet(mavenDependenciesList)
    assertThat(licenseSet).hasSize(3)
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

    val finalDepsList =
      retriever.updateMavenDependenciesList(
        mavenDependenciesList, manuallyUpdatedLicenses = setOf()
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

    val finalDepsList = retriever.updateMavenDependenciesList(
      mavenDependenciesList,
      setOf<License>(updatedLicense2)
    )
    assertThat(finalDepsList).hasSize(2)
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

    retriever.writeTextProto(
      "${tempFolder.root}/scripts/assets/maven_dependencies.textproto",
      mavenDependencyList
    )

    assertThat(textProtoFile.readText()).isEmpty()
  }

  @Test
  fun testWriteTextProto_nonEmptyMavenDependencyList_writesTextProtoFile() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")

    val license1 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
      this.directLinkOnly = DirectLinkOnly.newBuilder().apply {
        url = "https://developer.android.com/studio/terms.html"
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

    retriever.writeTextProto(
      "${tempFolder.root}/scripts/assets/maven_dependencies.textproto",
      mavenDependencyList
    )

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE,
      artifactVersion = FIREBASE_ANALYTICS_VERSION,
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasDirectLinkOnlyVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://developer.android.com/studio/terms.html",
      verifiedLink = "https://developer.android.com/studio/terms.html",
      licenseName = "Android Software Development Kit License"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION,
    )
    val licenseForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License"
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

    val brokenLicenses = retriever.getAllBrokenLicenses(mavenDependenciesList)
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

    val brokenLicenses = retriever.getAllBrokenLicenses(mavenDependenciesList)
    assertThat(brokenLicenses).hasSize(2)
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

    val brokenLicenses = retriever.getAllBrokenLicenses(mavenDependenciesList)
    assertThat(brokenLicenses).doesNotContain(license2)
  }

  @Test
  fun testfindFirstDepsWithBrokenLicenses_emptyBrokenLicenseSet_returnsEmptyMap() {
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

    val licenseToDepNameMap =
      retriever.findFirstDependenciesWithBrokenLicenses(
        mavenDependenciesList, brokenLicenses = setOf()
      )
    assertThat(licenseToDepNameMap).isEmpty()
  }

  @Test
  fun testfindFirstDepsWithBrokenLicenses_nonEmptyBrokenLicenseSet_returnsCorrectMap() {
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
        this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
        this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
        this.addLicense(license1)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE
        this.artifactVersion = FIREBASE_ANALYTICS_VERSION
        this.addLicense(license2)
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_NO_LICENSE
        this.artifactVersion = PROTO_LITE_VERSION
        this.addLicense(license2)
      }.build()
    )

    val licenseToDepNameMap =
      retriever.findFirstDependenciesWithBrokenLicenses(
        mavenDependenciesList,
        setOf<License>(license1, license2)
      )
    assertThat(licenseToDepNameMap).hasSize(2)
    assertThat(licenseToDepNameMap).containsEntry(license1, DEP_WITH_SCRAPABLE_LICENSE)
    assertThat(licenseToDepNameMap).doesNotContainEntry(
      license1,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
    )
    assertThat(licenseToDepNameMap).containsEntry(license2, DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    assertThat(licenseToDepNameMap).doesNotContainEntry(license2, DEP_WITH_NO_LICENSE)
  }

  @Test
  fun testGetDepsThatNeedIntervention_allDepsDetailsComplete_returnsEmptySet() {
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

    val depsThatNeedInterventionSet =
      retriever.getDependenciesThatNeedIntervention(mavenDependenciesList)
    assertThat(depsThatNeedInterventionSet).isEmpty()
  }

  @Test
  fun testGetDepsThatNeedIntervention_depsDetailsIncomplete_returnsCorrectDepsSet() {
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Fabric Software and Services Agreement"
      this.originalLink = "https://fabric.io/terms"
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
      }.build(),
      MavenDependency.newBuilder().apply {
        this.artifactName = DEP_WITH_INVALID_LINKS
        this.artifactVersion = IO_FABRIC_VERSION
        this.addLicense(license2)
      }.build()
    )

    val depsThatNeedInterventionSet =
      retriever.getDependenciesThatNeedIntervention(mavenDependenciesList)
    assertThat(depsThatNeedInterventionSet).hasSize(2)
    assertIsDependency(
      dependency = depsThatNeedInterventionSet.elementAt(0),
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    assertIsDependency(
      dependency = depsThatNeedInterventionSet.elementAt(1),
      artifactName = DEP_WITH_INVALID_LINKS,
      artifactVersion = IO_FABRIC_VERSION
    )
  }

  @Test
  fun testRetrieveMavenDepList_emptyPbFile_returnsEmptyDepsList() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val mavenDependencyList = MavenDependencyList.newBuilder().build()

    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val mavenDependenciesList = retriever.retrieveMavenDependencyList(
      "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
    )

    assertThat(mavenDependenciesList).isEmpty()
  }

  @Test
  fun testRetrieveMavenDepList_nonEmptyMavenDependencyList_returnsCorrectDepsList() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

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

    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val mavenDependenciesList = retriever.retrieveMavenDependencyList(
      "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
    )

    assertThat(mavenDependenciesList).isEqualTo(mavenDependencyList.mavenDependencyList)
    assertIsDependency(
      dependency = mavenDependenciesList[0],
      artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE,
      artifactVersion = FIREBASE_ANALYTICS_VERSION
    )
    assertThat(mavenDependenciesList[0].licenseList.size).isEqualTo(1)
    verifyLicenseHasScrapableVerifiedLink(
      license = mavenDependenciesList[0].licenseList[0],
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    assertIsDependency(
      dependency = mavenDependenciesList[1],
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    assertThat(mavenDependenciesList[1].licenseList.size).isEqualTo(1)
    verifyLicenseHasVerifiedLinkNotSet(
      license = mavenDependenciesList[1].licenseList[0],
      licenseName = "Simplified BSD License",
      originalLink = "https://www.opensource.org/licenses/bsd-license"
    )
  }

  @Test
  fun testGetDepListFromMavenInstall_emptyBazelQueryDepsList_returnsEmptyDepList() {
    val mavenInstallFile = tempFolder.newFile("third_party/maven_install.json")
    writeMavenInstallJson(mavenInstallFile)

    val mavenListDependencies = runBlocking {
      retriever.generateDependenciesListFromMavenInstallAsync(
        "${tempFolder.root}/third_party/maven_install.json",
        listOf()
      ).await()
    }

    assertThat(mavenListDependencies).isEmpty()
  }

  @Test
  fun testGetDepListFromMavenInstall_commonBazelQueryDepsList_returnsCorrectDepsList() {
    val mavenInstallFile = tempFolder.newFile("third_party/maven_install.json")
    writeMavenInstallJson(mavenInstallFile)

    val mavenListDependencies = runBlocking {
      retriever.generateDependenciesListFromMavenInstallAsync(
        "${tempFolder.root}/third_party/maven_install.json",
        listOf(DATA_BINDING_DEP, FIREBASE_DEP)
      ).await()
    }

    assertThat(mavenListDependencies).containsExactly(
      MavenListDependency(
        coord = DEP_WITH_SCRAPABLE_LICENSE.coordStrToMavenCoord(),
        repoUrls = listOf(GOOGLE_MAVEN_URL)
      ),
      MavenListDependency(
        coord = DEP_WITH_DIRECT_LINK_ONLY_LICENSE.coordStrToMavenCoord(),
        repoUrls = listOf(GOOGLE_MAVEN_URL)
      )
    )
  }

  @Test
  fun testRetrieveDepListFromPom_emptyMavenListDependencies_returnsEmptyMavenDepList() {
    val mavenDependencyList = runBlocking {
      retriever.retrieveDependencyListFromPomAsync(listOf()).await()
    }

    assertThat(mavenDependencyList.mavenDependencyList).isEmpty()
  }

  @Test
  fun testRetrieveDepListFromPom_mixedDepTypes_returnsCorrectMavenDepList() {
    val mavenDependencyList = runBlocking {
      retriever.retrieveDependencyListFromPomAsync(
        listOf(
          MavenListDependency(
            coord = DEP_WITH_SCRAPABLE_LICENSE.coordStrToMavenCoord(),
            repoUrls = listOf(GOOGLE_MAVEN_URL)
          ),
          MavenListDependency(
            coord = DEP_WITH_NO_LICENSE.coordStrToMavenCoord(),
            repoUrls = listOf(PUBLIC_MAVEN_URL)
          )
        )
      ).await()
    }

    assertThat(mavenDependencyList.mavenDependencyList.size).isEqualTo(2)
    val dependency1 = mavenDependencyList.mavenDependencyList[0]
    val dependency2 = mavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    assertThat(dependency1.licenseList.size).isEqualTo(1)
    verifyLicenseHasVerifiedLinkNotSet(
      license = dependency1.licenseList[0],
      licenseName = "The Apache License, Version 2.0",
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION
    )
    assertThat(dependency2.licenseList).isEmpty()
  }

  @Test
  fun testGenerateDepsListFromMavenInstall_emptyBazelQueryDeps_returnsEmptyList() {
    val mavenInstallFile = tempFolder.newFile("third_party/maven_install.json")
    writeMavenInstallJson(mavenInstallFile)

    val mavenListDependencies = runBlocking {
      retriever.generateDependenciesListFromMavenInstallAsync(
        "${tempFolder.root}/third_party/maven_install.json",
        listOf()
      ).await()
    }

    assertThat(mavenListDependencies).isEmpty()
  }

  @Test
  fun testGenerateDepsListFromMavenInstall_nonEmptyBazelQueryDepNames_returnsCorrectList() {
    val mavenInstallFile = tempFolder.newFile("third_party/maven_install.json")
    writeMavenInstallJson(mavenInstallFile)

    val mavenListDependencies = runBlocking {
      retriever.generateDependenciesListFromMavenInstallAsync(
        "${tempFolder.root}/third_party/maven_install.json",
        listOf(DATA_BINDING_DEP, FIREBASE_DEP)
      ).await()
    }

    assertThat(mavenListDependencies).containsExactly(
      MavenListDependency(
        coord = DEP_WITH_SCRAPABLE_LICENSE.coordStrToMavenCoord(),
        repoUrls = listOf(GOOGLE_MAVEN_URL)
      ),
      MavenListDependency(
        coord = DEP_WITH_DIRECT_LINK_ONLY_LICENSE.coordStrToMavenCoord(),
        repoUrls = listOf(GOOGLE_MAVEN_URL)
      )
    )
  }

  @Test
  fun testMavenCoordinate_parseFrom_oneComponent_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      MavenDependenciesRetriever.MavenCoordinate.parseFrom("androidx.lifecycle")
    }

    assertThat(exception).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testMavenCoordinate_parseFrom_twoComponents_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      MavenDependenciesRetriever.MavenCoordinate.parseFrom("androidx.lifecycle:lifecycle-viewmodel")
    }

    assertThat(exception).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testMavenCoordinate_parseFrom_threeComponents_returnsCoordinateWithGroupArtifactVersion() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate.parseFrom(
        "androidx.lifecycle:lifecycle-viewmodel:2.2.0"
      )

    assertThat(coord.groupId).isEqualTo("androidx.lifecycle")
    assertThat(coord.artifactId).isEqualTo("lifecycle-viewmodel")
    assertThat(coord.version).isEqualTo("2.2.0")
    assertThat(coord.classifier).isNull()
    assertThat(coord.extension).isNull()
  }

  @Test
  fun testMavenCoordinate_parseFrom_fourComponents_returnsCoordinateWithExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate.parseFrom(
        "androidx.lifecycle:lifecycle-viewmodel:aar:2.2.0"
      )

    assertThat(coord.groupId).isEqualTo("androidx.lifecycle")
    assertThat(coord.artifactId).isEqualTo("lifecycle-viewmodel")
    assertThat(coord.version).isEqualTo("2.2.0")
    assertThat(coord.classifier).isNull()
    assertThat(coord.extension).isEqualTo("aar")
  }

  @Test
  fun testMavenCoordinate_parseFrom_fiveComponents_returnsCoordinateWithClassifierAndExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate.parseFrom(
        "androidx.lifecycle:lifecycle-viewmodel:aar:sources:2.2.0"
      )

    assertThat(coord.groupId).isEqualTo("androidx.lifecycle")
    assertThat(coord.artifactId).isEqualTo("lifecycle-viewmodel")
    assertThat(coord.version).isEqualTo("2.2.0")
    assertThat(coord.classifier).isEqualTo("sources")
    assertThat(coord.extension).isEqualTo("aar")
  }

  @Test
  fun testMavenCoordinate_parseFrom_sixComponents_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      MavenDependenciesRetriever.MavenCoordinate.parseFrom(
        "androidx.lifecycle:lifecycle-viewmodel:aar:sources:fake:2.2.0"
      )
    }

    assertThat(exception).hasMessageThat().contains("Invalid Maven coordinate string")
  }

  @Test
  fun testMavenCoordinate_reducedCoordinateString_simpleCoordinate_returnsCorrectValue() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0"
      )

    val reducedCoordStr = coord.reducedCoordinateString

    // The group ID, artifact ID, and version should all be included in a reduced coordinate string.
    assertThat(reducedCoordStr).isEqualTo("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
  }

  @Test
  fun testMavenCoordinate_reducedCoordinateString_coordWithExtension_returnsCoordStrNoExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar"
      )

    val reducedCoordStr = coord.reducedCoordinateString

    // The extension is ignored in the reduced coordinate string.
    assertThat(reducedCoordStr).isEqualTo("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
  }

  @Test
  fun testMavenCoordinate_reducedCoordinateString_coordWithClassifier_returnsCoordStrNoClass() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        classifier = "sources"
      )

    val reducedCoordStr = coord.reducedCoordinateString

    // The classifier is ignored in the reduced coordinate string.
    assertThat(reducedCoordStr).isEqualTo("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
  }

  @Test
  fun testMavenCoordinate_reducedCoordinateString_coordWithClassAndExt_returnsStrWithoutBoth() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar",
        classifier = "sources"
      )

    val reducedCoordStr = coord.reducedCoordinateString

    // Both the extension and classifier are ignored in the reduced coordinate string.
    assertThat(reducedCoordStr).isEqualTo("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
  }

  @Test
  fun testMavenCoordinate_bazelTarget_simpleCoordinate_returnsTargetIgnoringVersion() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0"
      )

    val bazelTarget = coord.bazelTarget

    // Only the group & artifact IDs should be included in the base Bazel target.
    assertThat(bazelTarget).isEqualTo("androidx_lifecycle_lifecycle_viewmodel")
  }

  @Test
  fun testMavenCoordinate_bazelTarget_coordWithExtension_returnsTargetIgnoringExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar"
      )

    val bazelTarget = coord.bazelTarget

    // The extension is ignored in the base Bazel target.
    assertThat(bazelTarget).isEqualTo("androidx_lifecycle_lifecycle_viewmodel")
  }

  @Test
  fun testMavenCoordinate_bazelTarget_coordWithClassifier_returnsTargetIgnoringClassifier() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        classifier = "sources"
      )

    val bazelTarget = coord.bazelTarget

    // The classifier is ignored in the base Bazel target.
    assertThat(bazelTarget).isEqualTo("androidx_lifecycle_lifecycle_viewmodel")
  }

  @Test
  fun testMavenCoordinate_bazelTarget_coordWithClassAndExt_returnsTargetIgnoringBoth() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar",
        classifier = "sources"
      )

    val bazelTarget = coord.bazelTarget

    // Both the extension and classifier are ignored the base Bazel target.
    assertThat(bazelTarget).isEqualTo("androidx_lifecycle_lifecycle_viewmodel")
  }

  @Test
  fun testMavenCoordinate_computeArtifactUrl_simpleCoordinate_returnsCorrectMavenUrl() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0"
      )

    val artifactUrl = coord.computeArtifactUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the artifact URL. If
    // there's no extension defined, it should default to 'jar' per:
    // https://maven.apache.org/repositories/artifacts.html.
    assertThat(artifactUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0.jar"
    )
  }

  @Test
  fun testMavenCoordinate_computeArtifactUrl_coordWithExtension_returnsUrlWithExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar"
      )

    val artifactUrl = coord.computeArtifactUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the artifact URL.
    assertThat(artifactUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0.aar"
    )
  }

  @Test
  fun testMavenCoordinate_computeArtifactUrl_coordWithClassifier_returnsUrlWithClassifier() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        classifier = "sources"
      )

    val artifactUrl = coord.computeArtifactUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the artifact URL.
    assertThat(artifactUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0-sources.jar"
    )
  }

  @Test
  fun testMavenCoordinate_computeArtifactUrl_coordWithClassAndExt_returnsUrlWithBoth() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar",
        classifier = "sources"
      )

    val artifactUrl = coord.computeArtifactUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the artifact URL.
    assertThat(artifactUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0-sources.aar"
    )
  }

  @Test
  fun testMavenCoordinate_computePomUrl_simpleCoordinate_returnsCorrectMavenUrl() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0"
      )

    val pomUrl = coord.computePomUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the POM URL except for
    // extension since the ending of the URL is always 'pom'.
    assertThat(pomUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0.pom"
    )
  }

  @Test
  fun testMavenCoordinate_computePomUrl_coordWithExtension_returnsUrlWithExtension() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar"
      )

    val pomUrl = coord.computePomUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the POM URL except for
    // extension since the ending of the URL is always 'pom'.
    assertThat(pomUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0.pom"
    )
  }

  @Test
  fun testMavenCoordinate_computePomUrl_coordWithClassifier_returnsUrlWithClassifier() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        classifier = "sources"
      )

    val pomUrl = coord.computePomUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the POM URL except for
    // extension since the ending of the URL is always 'pom'.
    assertThat(pomUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0-sources.pom"
    )
  }

  @Test
  fun testMavenCoordinate_computePomUrl_coordWithClassAndExt_returnsUrlWithBoth() {
    val coord =
      MavenDependenciesRetriever.MavenCoordinate(
        groupId = "androidx.lifecycle",
        artifactId = "lifecycle-viewmodel",
        version = "2.2.0",
        extension = "aar",
        classifier = "sources"
      )

    val pomUrl = coord.computePomUrl("https://maven.google.com")

    // All properties of the coordinate should be included when computing the POM URL except for
    // extension since the ending of the URL is always 'pom'.
    assertThat(pomUrl).isEqualTo(
      "https://maven.google.com/androidx/lifecycle/lifecycle-viewmodel/2.2.0" +
        "/lifecycle-viewmodel-2.2.0-sources.pom"
    )
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

  private fun writeThirdPartyBuildFile(
    coordsList: List<String>,
    thirdPartyPrefixCoordList: List<String>
  ) {
    val thirdPartyBuild = tempFolder.newFile("third_party/BUILD.bazel")
    thirdPartyBuild.appendText(
      """
      load("@rules_jvm_external//:defs.bzl", "artifact")
      """.trimIndent() + "\n"
    )
    coordsList.forEachIndexed { index, coord ->
      createThirdPartyAndroidLibrary(
        thirdPartyBuild = thirdPartyBuild,
        coord = coord,
        artifactName = thirdPartyPrefixCoordList[index].substringAfter(':')
      )
    }
  }

  private fun createThirdPartyAndroidLibrary(
    thirdPartyBuild: File,
    coord: String,
    artifactName: String
  ) {
    thirdPartyBuild.appendText(
      """
      android_library(
          name = "$artifactName",
          visibility = ["//visibility:public"],
          exports = [artifact("$coord")],
      )
      """.trimIndent() + "\n"
    )
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
        "artifacts": {
          "androidx.databinding:databinding-adapters": {
            "version": "3.4.2"
          },
          "com.github.bumptech.glide:annotations": {
            "version": "4.11.0"
          },
          "com.google.firebase:firebase-analytics": {
            "version": "17.5.0"
          },
          "com.google.protobuf:protobuf-lite": {
            "version": "3.0.0"
          },
          "io.fabric.sdk.android:fabric": {
            "version": "1.4.7"
          }
        },
        "repositories": {
          "$GOOGLE_MAVEN_URL": [
            "androidx.databinding:databinding-adapters",
            "com.google.firebase:firebase-analytics",
            "io.fabric.sdk.android:fabric"
          ],
          "$PUBLIC_MAVEN_URL": [
            "com.github.bumptech.glide:annotations",
            "com.google.protobuf:protobuf-lite"
          ]
        }
      }
      """.trimIndent()
    )
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }

  /** Returns a mock for the [MavenArtifactPropertyFetcher]. */
  private fun initializeArtifactPropertyFetcher(): MavenArtifactPropertyFetcher {
    return mock {
      on { scrapeText(eq(DATA_BINDING_POM_URL)) }
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
      on { scrapeText(eq(GLIDE_ANNOTATIONS_POM_URL)) }
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
      on { scrapeText(eq(FIREBASE_ANALYTICS_POM_URL)) }
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
      on { scrapeText(eq(IO_FABRIC_POM_URL)) }
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
      on { scrapeText(eq(PROTO_LITE_POM_URL)) }
        .doReturn(
          """
          <?xml version="1.0" encoding="UTF-8"?>
          <project>Random Project</project>
          """.trimIndent()
        )
      on { isValidArtifactFileUrl(eq(DATA_BINDING_ARTIFACT_URL)) }.thenReturn(true)
      on { isValidArtifactFileUrl(eq(PROTO_LITE_ARTIFACT_URL)) }.thenReturn(true)
      on { isValidArtifactFileUrl(eq(IO_FABRIC_ARTIFACT_URL)) }.thenReturn(true)
      on { isValidArtifactFileUrl(eq(GLIDE_ANNOTATIONS_ARTIFACT_URL)) }.thenReturn(true)
      on { isValidArtifactFileUrl(eq(FIREBASE_ANALYTICS_ARTIFACT_URL)) }.thenReturn(true)
    }
  }

  private fun initializeMavenDependenciesRetriever(): MavenDependenciesRetriever {
    return MavenDependenciesRetriever(
      "${tempFolder.root}",
      mockArtifactPropertyFetcher,
      ScriptBackgroundCoroutineDispatcher(),
      commandExecutor
    )
  }

  private fun String.coordStrToMavenCoord() =
    MavenDependenciesRetriever.MavenCoordinate.parseFrom(this)

  private companion object {
    private const val DEP_WITH_SCRAPABLE_LICENSE = "androidx.databinding:databinding-adapters:3.4.2"
    private const val DEP_WITH_NO_LICENSE = "com.google.protobuf:protobuf-lite:3.0.0"
    private const val DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES =
      "com.github.bumptech.glide:annotations:4.11.0"
    private const val DEP_WITH_DIRECT_LINK_ONLY_LICENSE =
      "com.google.firebase:firebase-analytics:17.5.0"
    private const val DEP_WITH_INVALID_LINKS = "io.fabric.sdk.android:fabric:1.4.7"

    private const val DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX =
      "//third_party:androidx_databinding_databinding-adapters"
    private const val FIREBASE_DEP_WITH_THIRD_PARTY_PREFIX =
      "//third_party:com_google_firebase_firebase-analytics"
    private const val IO_FABRIC_DEP_WITH_THIRD_PARTY_PREFIX =
      "//third_party:io_fabric_sdk_android_fabric"

    private const val DATA_BINDING_DEP = "androidx_databinding_databinding_adapters"
    private const val FIREBASE_DEP = "com_google_firebase_firebase_analytics"
    private const val IO_FABRIC_DEP = "io_fabric_sdk_android_fabric"

    private const val GOOGLE_MAVEN_URL = "https://maven.google.com"
    private const val PUBLIC_MAVEN_URL = "https://repo1.maven.org/maven2"

    private const val DATA_BINDING_VERSION = "3.4.2"
    private const val DATA_BINDING_BASE_URL =
      "$GOOGLE_MAVEN_URL/androidx/databinding/databinding-adapters" +
        "/$DATA_BINDING_VERSION/databinding-adapters-$DATA_BINDING_VERSION"
    private const val DATA_BINDING_ARTIFACT_URL = "$DATA_BINDING_BASE_URL.jar"
    private const val DATA_BINDING_POM_URL = "$DATA_BINDING_BASE_URL.pom"

    private const val PROTO_LITE_VERSION = "3.0.0"
    private const val PROTO_LITE_BASE_URL =
      "$PUBLIC_MAVEN_URL/com/google/protobuf/protobuf-lite/$PROTO_LITE_VERSION" +
        "/protobuf-lite-$PROTO_LITE_VERSION"
    private const val PROTO_LITE_POM_URL = "$PROTO_LITE_BASE_URL.pom"
    private const val PROTO_LITE_ARTIFACT_URL = "$PROTO_LITE_BASE_URL.jar"

    private const val IO_FABRIC_VERSION = "1.4.7"
    private const val IO_FABRIC_BASE_URL =
      "$GOOGLE_MAVEN_URL/io/fabric/sdk/android/fabric/$IO_FABRIC_VERSION/fabric-$IO_FABRIC_VERSION"
    private const val IO_FABRIC_POM_URL = "$IO_FABRIC_BASE_URL.pom"
    private const val IO_FABRIC_ARTIFACT_URL = "$IO_FABRIC_BASE_URL.jar"

    private const val GLIDE_ANNOTATIONS_VERSION = "4.11.0"
    private const val GLIDE_ANNOTATIONS_BASE_URL =
      "$PUBLIC_MAVEN_URL/com/github/bumptech/glide/annotations/$GLIDE_ANNOTATIONS_VERSION" +
        "/annotations-$GLIDE_ANNOTATIONS_VERSION"
    private const val GLIDE_ANNOTATIONS_POM_URL = "$GLIDE_ANNOTATIONS_BASE_URL.pom"
    private const val GLIDE_ANNOTATIONS_ARTIFACT_URL = "$GLIDE_ANNOTATIONS_BASE_URL.jar"

    private const val FIREBASE_ANALYTICS_VERSION = "17.5.0"
    private const val FIREBASE_ANALYTICS_BASE_URL =
      "$GOOGLE_MAVEN_URL/com/google/firebase/firebase-analytics/$FIREBASE_ANALYTICS_VERSION" +
        "/firebase-analytics-$FIREBASE_ANALYTICS_VERSION"
    private const val FIREBASE_ANALYTICS_POM_URL = "$FIREBASE_ANALYTICS_BASE_URL.pom"
    private const val FIREBASE_ANALYTICS_ARTIFACT_URL = "$FIREBASE_ANALYTICS_BASE_URL.jar"
  }
}
