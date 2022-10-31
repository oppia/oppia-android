package org.oppia.android.scripts.license

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
import org.oppia.android.scripts.common.CommandExecutor

/** Tests for [MavenDependenciesListCheck]. */
class MavenDependenciesListCheckTest {

  private val DATA_BINDING_DEP = "androidx.databinding:databinding-adapters:3.4.2"
  private val PROTO_LITE_DEP = "com.google.protobuf:protobuf-lite:3.0.0"
  private val GLIDE_DEP =
    "com.github.bumptech.glide:annotations:4.11.0"
  private val FIREBASE_ANALYTICS_DEP = "com.google.firebase:firebase-analytics:17.5.0"
  private val IO_FABRIC_DEP = "io.fabric.sdk.android:fabric:1.4.7"
  private val FIREBASE_ANALYTICS_UPGRADED_DEP =
    "com.google.firebase:firebase-analytics:19.0.0"

  private val DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX =
    "//third_party:androidx_databinding_databinding-adapters"
  private val PROTO_DEP_WITH_THIRD_PARTY_PREFIX =
    "//third_party:com_google_protobuf_protobuf-javalite"
  private val GLIDE_DEP_WITH_THIRD_PARTY_PREFIX =
    "//third_party:com_github_bumptech_glide_annotations"
  private val FIREBASE_DEP_WITH_THIRD_PARTY_PREFIX =
    "//third_party:com_google_firebase_firebase-analytics"
  private val IO_FABRIC_DEP_WITH_THIRD_PARTY_PREFIX =
    "//third_party:io_fabric_sdk_android_fabric"

  private val DATA_BINDING_VERSION = "3.4.2"
  private val PROTO_LITE_VERSION = "3.0.0"
  private val GLIDE_ANNOTATIONS_VERSION = "4.11.0"
  private val FIREBASE_ANALYTICS_VERSION = "17.5.0"
  private val FIREBASE_ANALYTICS_UPGRADED_VERSION = "19.0.0"
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
  private val UPGRADED_FIREBASE_ANALYTICS_POM = "https://maven.google.com/com/google/firebase/" +
    "firebase-analytics/$FIREBASE_ANALYTICS_UPGRADED_VERSION/firebase-analytics-" +
    "$FIREBASE_ANALYTICS_UPGRADED_VERSION.pom"

  private val SCRIPT_PASSED_MESSAGE =
    "maven_dependencies.textproto is up-to-date."
  private val MISSING_DEPENDENCIES_ONLY_FAILURE =
    "Missing dependencies in maven_dependencies.textproto"
  private val REDUNDANT_DEPENDENCIES_ONLY_FAILURE =
    "Redundant dependencies in maven_dependencies.textproto"
  private val MISSING_AND_REDUNDANT_DEPENDENCIES_FAILURE =
    "Redundant and missing dependencies in maven_dependencies.textproto"
  private val LICENSE_DETAILS_INCOMPLETE_FAILURE = "Licenses details are not completed"
  private val UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE =
    "License links are invalid or not available for some dependencies"

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }
  private val commandExecutorBuilder by lazy { initializeExecutorBuilderWithLongProcessWaitTime() }
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
  fun testMavenDepsListCheck_emptyPbFile_failsAndCallsOutMissingDeps() {
    tempFolder.newFile("scripts/assets/maven_dependencies.pb")

    val coordsList = listOf(DATA_BINDING_DEP, FIREBASE_ANALYTICS_DEP)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_DEPENDENCIES_ONLY_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Missing dependencies that need to be added:

      artifact_name: "$DATA_BINDING_DEP"
      artifact_version: "$DATA_BINDING_VERSION"
      license {
        license_name: "The Apache License, Version 2.0"
        original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
      
      artifact_name: "com.google.firebase:firebase-analytics:17.5.0"
      artifact_version: "17.5.0"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_singleDepAdded_failsAndCallsOutMissingDep() {
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
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }
    val coordsList = listOf(
      DATA_BINDING_DEP,
      GLIDE_DEP,
      FIREBASE_ANALYTICS_DEP,
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_DEPENDENCIES_ONLY_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Missing dependencies that need to be added:

      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_multipleDepsAdded_failsAndCallsOutMissingDeps() {
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
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }
    val coordsList = listOf(
      DATA_BINDING_DEP,
      GLIDE_DEP,
      FIREBASE_ANALYTICS_DEP,
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_DEPENDENCIES_ONLY_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Missing dependencies that need to be added:

      artifact_name: "$DATA_BINDING_DEP"
      artifact_version: "$DATA_BINDING_VERSION"
      license {
        license_name: "The Apache License, Version 2.0"
        original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
      
      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_singleDepRemoved_failsAndCallsOutRedundantDep() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "Terms of Service for Firebase Services"
      this.originalLink = "https://fabric.io/terms"
      this.directLinkOnly = DirectLinkOnly.newBuilder().apply {
        url = "https://firebase.google.com/terms"
      }.build()
      this.isOriginalLinkInvalid = true
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
            this.artifactName = IO_FABRIC_DEP
            this.artifactVersion = IO_FABRIC_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }
    val coordsList = listOf(GLIDE_DEP)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(REDUNDANT_DEPENDENCIES_ONLY_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Redundant dependencies that need to be removed:

      artifact_name: "$IO_FABRIC_DEP"
      artifact_version: "$IO_FABRIC_VERSION"
      license {
        license_name: "Terms of Service for Firebase Services"
        original_link: "https://fabric.io/terms"
        direct_link_only {
          url: "https://firebase.google.com/terms"
        }
        is_original_link_invalid: true
      }

      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_multipleDepsRemoved_failsAndCallsOutRedundantDeps() {
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
    val license3 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = FIREBASE_ANALYTICS_DEP
            this.artifactVersion = FIREBASE_ANALYTICS_VERSION
            this.addLicense(license3)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }
    val coordsList = listOf(GLIDE_DEP)
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(REDUNDANT_DEPENDENCIES_ONLY_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Redundant dependencies that need to be removed:

      artifact_name: "$DATA_BINDING_DEP"
      artifact_version: "$DATA_BINDING_VERSION"
      license {
        license_name: "The Apache License, Version 2.0"
        original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
      
      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }

      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_depsRemovedAndAddedBoth_failsAndCallOutRedundantAndMissingDeps() {
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
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }
    val coordsList = listOf(
      GLIDE_DEP,
      FIREBASE_ANALYTICS_DEP
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_AND_REDUNDANT_DEPENDENCIES_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Redundant dependencies that need to be removed:

      artifact_name: "$DATA_BINDING_DEP"
      artifact_version: "$DATA_BINDING_VERSION"
      license {
        license_name: "The Apache License, Version 2.0"
        original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }

      Missing dependencies that need to be added:
      
      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_depVersionUpgraded_failsWithException() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = FIREBASE_ANALYTICS_DEP
            this.artifactVersion = FIREBASE_ANALYTICS_VERSION
            this.addLicense(license2)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DATA_BINDING_DEP,
      FIREBASE_ANALYTICS_UPGRADED_DEP
    )
    setUpBazelEnvironmentWithUpdatedFirebaseDependency(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_AND_REDUNDANT_DEPENDENCIES_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Redundant dependencies that need to be removed:

      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }

      Missing dependencies that need to be added:
      
      artifact_name: "$FIREBASE_ANALYTICS_UPGRADED_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_UPGRADED_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_depVersionDowngraded_failsWithException() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = FIREBASE_ANALYTICS_UPGRADED_DEP
            this.artifactVersion = FIREBASE_ANALYTICS_UPGRADED_VERSION
            this.addLicense(license2)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DATA_BINDING_DEP,
      FIREBASE_ANALYTICS_UPGRADED_DEP
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MISSING_AND_REDUNDANT_DEPENDENCIES_FAILURE)
    assertThat(outContent.toString()).isEqualTo(
      """ 
      Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.

      Redundant dependencies that need to be removed:

      artifact_name: "$FIREBASE_ANALYTICS_UPGRADED_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_UPGRADED_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }

      Missing dependencies that need to be added:
      
      artifact_name: "$FIREBASE_ANALYTICS_DEP"
      artifact_version: "$FIREBASE_ANALYTICS_VERSION"
      license {
        license_name: "Android Software Development Kit License"
        original_link: "https://developer.android.com/studio/terms.html"
      }
      
      Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
      """.trimIndent() + "\n"
    )
  }

  @Test
  fun testMavenDepsListCheck_allDepsUpToDate_checkPasses() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()
    val license2 = License.newBuilder().apply {
      this.licenseName = "Android Software Development Kit License"
      this.originalLink = "https://developer.android.com/studio/terms.html"
      this.directLinkOnly = DirectLinkOnly.newBuilder().apply {
        url = "https://developer.android.com/studio/terms.html"
      }.build()
    }.build()
    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = FIREBASE_ANALYTICS_DEP
            this.artifactVersion = FIREBASE_ANALYTICS_VERSION
            this.addLicense(license2)
          }.build()
        )
      )
    }.build()
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val coordsList = listOf(
      DATA_BINDING_DEP,
      FIREBASE_ANALYTICS_UPGRADED_DEP
    )
    setUpBazelEnvironment(coordsList)

    MavenDependenciesListCheck(
      mockLicenseFetcher,
      commandExecutorBuilder
    ).main(
      arrayOf(
        "${tempFolder.root}",
        "scripts/assets/maven_install.json",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_MESSAGE)
  }

  @Test
  fun testMavenDepsListCheck_licenseLinkNotVerified_failsWithException() {
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
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = GLIDE_DEP
            this.artifactVersion = GLIDE_ANNOTATIONS_VERSION
            this.addAllLicense(listOf(license1, license2))
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(
      DATA_BINDING_DEP,
      GLIDE_DEP
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)
  }

  @Test
  fun testMavenDepsListCheck_depMissingLicenses_failsWithException() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val license1 = License.newBuilder().apply {
      this.licenseName = "The Apache License, Version 2.0"
      this.originalLink = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      this.scrapableLink = ScrapableLink.newBuilder().apply {
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }.build()
    }.build()

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = PROTO_LITE_DEP
            this.artifactVersion = PROTO_LITE_VERSION
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(
      DATA_BINDING_DEP,
      PROTO_LITE_DEP
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)
  }

  @Test
  fun testMavenDepsListCheck_depWithInvalidLicenseLink_failsWithException() {
    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
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

    val mavenDependencyList = MavenDependencyList.newBuilder().apply {
      this.addAllMavenDependency(
        listOf(
          MavenDependency.newBuilder().apply {
            this.artifactName = DATA_BINDING_DEP
            this.artifactVersion = DATA_BINDING_VERSION
            this.addLicense(license1)
          }.build(),
          MavenDependency.newBuilder().apply {
            this.artifactName = IO_FABRIC_DEP
            this.artifactVersion = IO_FABRIC_VERSION
            this.addLicense(license2)
          }.build()
        )
      )
    }.build()
    mavenDependencyList.writeTo(pbFile.outputStream())

    val coordsList = listOf(
      DATA_BINDING_DEP,
      IO_FABRIC_DEP
    )
    setUpBazelEnvironment(coordsList)

    val exception = assertThrows(Exception::class) {
      MavenDependenciesListCheck(
        mockLicenseFetcher,
        commandExecutorBuilder
      ).main(
        arrayOf(
          "${tempFolder.root}",
          "scripts/assets/maven_install.json",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(UNAVAILABLE_OR_INVALID_LICENSE_LINKS_FAILURE)
  }

  private fun setUpBazelEnvironment(coordsList: List<String>) {
    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
    writeMavenInstallJson(mavenInstallJson)
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      when (coordinate) {
        DATA_BINDING_DEP -> DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX
        FIREBASE_ANALYTICS_DEP, FIREBASE_ANALYTICS_UPGRADED_DEP ->
          FIREBASE_DEP_WITH_THIRD_PARTY_PREFIX
        IO_FABRIC_DEP -> IO_FABRIC_DEP_WITH_THIRD_PARTY_PREFIX
        GLIDE_DEP -> GLIDE_DEP_WITH_THIRD_PARTY_PREFIX
        else -> PROTO_DEP_WITH_THIRD_PARTY_PREFIX
      }
    }
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList, thirdPartyPrefixCoordList)
  }

  private fun setUpBazelEnvironmentWithUpdatedFirebaseDependency(coordsList: List<String>) {
    val mavenInstallJson = tempFolder.newFile("scripts/assets/maven_install.json")
    writeMavenInstallJson(
      mavenInstallJsonFile = mavenInstallJson,
      firebaseAnalyticsCoord = FIREBASE_ANALYTICS_UPGRADED_DEP,
      firebaseAnalayticsPom = UPGRADED_FIREBASE_ANALYTICS_POM
    )
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(coordsList)
    val thirdPartyPrefixCoordList = coordsList.map { coordinate ->
      when (coordinate) {
        DATA_BINDING_DEP -> DATA_BINDING_DEP_WITH_THIRD_PARTY_PREFIX
        FIREBASE_ANALYTICS_DEP, FIREBASE_ANALYTICS_UPGRADED_DEP ->
          FIREBASE_DEP_WITH_THIRD_PARTY_PREFIX
        IO_FABRIC_DEP -> IO_FABRIC_DEP_WITH_THIRD_PARTY_PREFIX
        GLIDE_DEP -> GLIDE_DEP_WITH_THIRD_PARTY_PREFIX
        else -> PROTO_DEP_WITH_THIRD_PARTY_PREFIX
      }
    }
    createThirdPartyAndroidBinary(thirdPartyPrefixCoordList)
    writeThirdPartyBuildFile(coordsList, thirdPartyPrefixCoordList)
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
  private fun writeMavenInstallJson(
    mavenInstallJsonFile: File,
    firebaseAnalyticsCoord: String = FIREBASE_ANALYTICS_DEP,
    firebaseAnalayticsPom: String = FIREBASE_ANALYTICS_POM
  ) {
    mavenInstallJsonFile.writeText(
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
              "coord": "$firebaseAnalyticsCoord",
              "url": "${firebaseAnalayticsPom.dropLast(3)}aar"
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

  private fun initializeExecutorBuilderWithLongProcessWaitTime(): CommandExecutor.Builder {
    val builder = CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder()
    return builder.setProcessTimeout(timeout = 5, timeoutUnit = TimeUnit.MINUTES)
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
      on { scrapeText(eq(UPGRADED_FIREBASE_ANALYTICS_POM)) }
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
