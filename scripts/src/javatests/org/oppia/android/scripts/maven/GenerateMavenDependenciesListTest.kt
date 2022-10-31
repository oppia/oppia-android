package org.oppia.android.scripts.maven

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
import org.oppia.android.scripts.license.LicenseFetcher
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

/** Tests for [GenerateMavenDependenciesList]. */
class GenerateMavenDependenciesListTest {

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
  fun testEmptyPbFile_scriptFailsWithException_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION,
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE,
      artifactVersion = FIREBASE_ANALYTICS_VERSION,
    )
    val licenseForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency2,
      originalLink = "https://developer.android.com/studio/terms.html",
      licenseName = "Android Software Development Kit License"
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
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
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
  fun testDependencyHasNonScrapableLink_scriptFailsWithException_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_DIRECT_LINK_ONLY_LICENSE)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency,
      artifactName = DEP_WITH_DIRECT_LINK_ONLY_LICENSE,
      artifactVersion = FIREBASE_ANALYTICS_VERSION,
    )
    val licenseForDependency = dependency.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency,
      originalLink = "https://developer.android.com/studio/terms.html",
      licenseName = "Android Software Development Kit License"
    )
  }

  @Test
  fun testDependencyHasExtractedCopyLinkAndScrapableLink_scriptFails_andWritesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency = dependency.licenseList[0]
    val license2ForDependency = dependency.licenseList[1]

    verifyLicenseHasVerifiedLinkNotSet(
      license = license1ForDependency,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License"
    )
    verifyLicenseHasVerifiedLinkNotSet(
      license = license2ForDependency,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0"
    )
  }

  @Test
  fun testDependencyHasInvalidLicense_scriptFailsWithException_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
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
            this.addLicense(license1)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(DEP_WITH_INVALID_LINKS)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency,
      artifactName = DEP_WITH_INVALID_LINKS,
      artifactVersion = IO_FABRIC_VERSION,
    )
    val licenseForDependency = dependency.licenseList[0]
    verifyLicenseHasOriginalLinkInvalid(
      license = licenseForDependency,
      originalLink = "https://fabric.io/terms",
      licenseName = "Fabric Software and Services Agreement"
    )
  }

  @Test
  fun testDependencyHasNoLicense_scriptFails_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DEP_WITH_NO_LICENSE)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency,
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION,
    )
    assertThat(dependency.licenseList).isEmpty()
  }

  @Test
  fun testDependenciesHaveMultipleLicense_completeLicenseDetails_scriptPasses_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
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
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList =
      listOf(DEP_WITH_SCRAPABLE_LICENSE, DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES)
    setUpBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION,
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testDependenciesHaveCompleteLicenseDetails_scriptPasses_writesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val license1 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
      this.directLinkOnly = DirectLinkOnly.newBuilder().apply {
        url = "https://developer.android.com/studio/terms.html"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
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

    val coordsList = listOf(DEP_WITH_DIRECT_LINK_ONLY_LICENSE, DEP_WITH_NO_LICENSE)
    setUpBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )

    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)

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
      licenseName = "Android Software Development Kit License",
      verifiedLink = "https://developer.android.com/studio/terms.html"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_NO_LICENSE,
      artifactVersion = PROTO_LITE_VERSION,
    )
    val licenseForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = licenseForDependency2,
      originalLink = "",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testLicensesIncomplete_noUpdatesProvided_scriptFails() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val incompleteApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val incompleteApacheLicenseWithSameOriginalLink = License.newBuilder().apply {
      this.licenseName = "The Apache Software License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeBsdLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(incompleteApacheLicense)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(incompleteApacheLicense, completeBsdLicense))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
            this.artifactVersion = MOSHI_VERSION
            this.addLicense(incompleteApacheLicenseWithSameOriginalLink)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
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
  fun testOneLicenseIncomplete_updatesAtOnePlace_scriptPassesAndUpdatesOtherPlaces() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val incompleteApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val completeGlideLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(completeApacheLicense)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(incompleteApacheLicense, completeGlideLicense))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
    )
    setUpBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )

    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
  }

  @Test
  fun testMultipleLicensesIncomplete_provideUpdates_scriptPassesAndUpdatesOtherPlaces() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val incompleteApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val incompleteApacheLicenseWithSameLink = License.newBuilder().apply {
      this.licenseName = "The Apache Software License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeGlideLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(completeApacheLicense)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(incompleteApacheLicense, completeGlideLicense))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
            this.artifactVersion = MOSHI_VERSION
            this.addLicense(incompleteApacheLicenseWithSameLink)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
    val dependency3 = outputMavenDependencyList.mavenDependencyList[2]
    assertIsDependency(
      dependency = dependency3,
      artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME,
      artifactVersion = MOSHI_VERSION,
    )
    val licenseForDependency3 = dependency3.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency3,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache Software License, Version 2.0",
    )
  }

  @Test
  fun testMultipleLicensesIncomplete_provideUpdatesAtOnePlace_scriptFailsAndUpdatesOtherPlaces() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val incompleteApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val incompleteApacheLicenseWithDifferentName = License.newBuilder().apply {
      this.licenseName = "The Apache Software License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }.build()
    val completeGlideLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(completeApacheLicense)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(incompleteApacheLicense, completeGlideLicense))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
            this.artifactVersion = MOSHI_VERSION
            this.addLicense(incompleteApacheLicenseWithDifferentName)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
    val dependency3 = outputMavenDependencyList.mavenDependencyList[2]
    assertIsDependency(
      dependency = dependency3,
      artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME,
      artifactVersion = MOSHI_VERSION,
    )
    val licenseForDependency3 = dependency3.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency3,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache Software License, Version 2.0",
    )
    assertThat(licenseForDependency1.licenseName).isNotEqualTo(licenseForDependency3.licenseName)
  }

  @Test
  fun testCompleteDepsList_newDepAddedWithNewLicense_scriptFailsAndUpdatesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val completeApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val completeGlideLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_LICENSE
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(completeApacheLicense)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(completeApacheLicense, completeGlideLicense))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      GenerateMavenDependenciesList(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "scripts/assets/maven_dependencies.textproto",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
    val dependency3 = outputMavenDependencyList.mavenDependencyList[2]
    assertIsDependency(
      dependency = dependency3,
      artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME,
      artifactVersion = MOSHI_VERSION,
    )
    val licenseForDependency3 = dependency3.licenseList[0]
    verifyLicenseHasVerifiedLinkNotSet(
      license = licenseForDependency3,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache Software License, Version 2.0",
    )
    assertThat(licenseForDependency1.licenseName).isNotEqualTo(licenseForDependency3.licenseName)
  }

  @Test
  fun testCompleteDepsList_newDepAddedWithExisitingLicense_scriptPassesAndUpdatesTextProto() {
    val textProtoFile = tempFolder.newFile("scripts/assets/maven_dependencies.textproto")
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val completeApacheLicense = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val completeApacheLicenseWithDifferentName = License.newBuilder().apply {
      this.licenseName = "The Apache Software License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val completeGlideLicense = License.newBuilder().apply {
      this.licenseName = "Simplified BSD License"
      this.originalLink = "https://www.opensource.org/licenses/bsd-license"
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().apply {
        url = "https://local-copy/bsd-license"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(completeApacheLicense, completeGlideLicense))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
            this.artifactVersion = MOSHI_VERSION
            this.addLicense(completeApacheLicenseWithDifferentName)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DEP_WITH_SCRAPABLE_LICENSE,
      DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME
    )
    setUpBazelEnvironment(coordsList)

    GenerateMavenDependenciesList(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "scripts/assets/maven_dependencies.textproto",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )

    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)

    val outputMavenDependencyList = parseTextProto(
      textProtoFile,
      MavenDependencyList.getDefaultInstance()
    )

    val dependency1 = outputMavenDependencyList.mavenDependencyList[0]
    assertIsDependency(
      dependency = dependency1,
      artifactName = DEP_WITH_SCRAPABLE_LICENSE,
      artifactVersion = DATA_BINDING_VERSION
    )
    val licenseForDependency1 = dependency1.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency1,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val dependency2 = outputMavenDependencyList.mavenDependencyList[1]
    assertIsDependency(
      dependency = dependency2,
      artifactName = DEP_WITH_SCRAPABLE_AND_EXTRACTED_COPY_LICENSES,
      artifactVersion = GLIDE_ANNOTATIONS_VERSION,
    )
    val license1ForDependency2 = dependency2.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = license1ForDependency2,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
    val license2ForDependency2 = dependency2.licenseList[1]
    verifyLicenseHasExtractedCopyVerifiedLink(
      license = license2ForDependency2,
      originalLink = "https://www.opensource.org/licenses/bsd-license",
      licenseName = "Simplified BSD License",
      verifiedLink = "https://local-copy/bsd-license"
    )
    val dependency3 = outputMavenDependencyList.mavenDependencyList[2]
    assertIsDependency(
      dependency = dependency3,
      artifactName = DEP_WITH_SAME_SCRAPABLE_LICENSE_BUT_DIFFERENT_NAME,
      artifactVersion = MOSHI_VERSION,
    )
    val licenseForDependency3 = dependency3.licenseList[0]
    verifyLicenseHasScrapableVerifiedLink(
      license = licenseForDependency3,
      originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt",
      licenseName = "The Apache Software License, Version 2.0",
      verifiedLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
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
      createThirdPartyAndroidLibrary(thirdPartyBuild, export)
    }
  }

  private fun createThirdPartyAndroidLibrary(thirdPartyBuild: File, artifactName: String) {
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
