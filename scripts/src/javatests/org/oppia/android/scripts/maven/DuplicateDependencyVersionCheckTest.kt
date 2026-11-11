package org.oppia.android.scripts.maven

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.DuplicateDependencyVersionExemptions
import org.oppia.android.scripts.proto.DuplicateDependencyVersionExemptions.DuplicateDependencyVersionExemption
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DuplicateDependencyVersionCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var repoDir: File

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
    repoDir = tempFolder.newFolder("repo")
    File(repoDir, "third_party").mkdirs()
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testCheck_versionsMatch_passes() {
    createVersionsFile(httpDependency("dagger", "2.41", "com.google.dagger:dagger"))
    createMavenInstallFile("com.google.dagger:dagger" to "2.41")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_versionsMatchWithMavenClassifier_passes() {
    createVersionsFile(
      httpDependency("guava_android", "31.0.1", "com.google.guava:guava"),
      httpDependency("guava_jre", "31.0.1", "com.google.guava:guava")
    )
    createMavenInstallFile("com.google.guava:guava" to "31.0.1-android")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_versionsDiffer_failsAndReportsBothVersions() {
    createVersionsFile(httpDependency("dagger", "2.41", "com.google.dagger:dagger"))
    createMavenInstallFile("com.google.dagger:dagger" to "2.44")

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains(
      "com.google.dagger:dagger is pulled in at 2.41 by the dagger HTTP dependency, but Maven" +
        " resolves it to 2.44."
    )
  }

  @Test
  fun testCheck_versionsDifferButExempted_passes() {
    createVersionsFile(httpDependency("dagger", "2.41", "com.google.dagger:dagger"))
    createMavenInstallFile("com.google.dagger:dagger" to "2.44")

    val checkPassed = runScript(
      createExemption(dependencyName = "dagger", reason = "Exempted for testing.")
    )

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_dependencyWithoutMavenCounterpart_isIgnored() {
    createVersionsFile(httpDependency("rules_kotlin", "v1.7.1", mavenCoordinate = null))
    createMavenInstallFile("com.google.dagger:dagger" to "2.41")

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_mavenArtifactWithPackagingSuffix_isMatched() {
    createVersionsFile(httpDependency("robolectric", "4.7", "org.robolectric:robolectric"))
    createMavenInstallFile("org.robolectric:robolectric:jar" to "4.9")

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains("org.robolectric:robolectric")
  }

  private fun createVersionsFile(vararg dependencies: Triple<String, String, String?>) {
    val dependencyEntries = dependencies.joinToString(separator = "\n") { (name, version, coord) ->
      val coordinateEntry = coord?.let { "\n|        \"maven_coordinate\": \"$it\"," } ?: ""
      """
      |    "$name": {$coordinateEntry
      |        "sha": "fakesha",
      |        "version": "$version",
      |    },
      """.trimMargin()
    }
    File(repoDir, "third_party/versions.bzl").writeText(
      """
      |HTTP_DEPENDENCY_VERSIONS = {
      |$dependencyEntries
      |}
      """.trimMargin()
    )
  }

  private fun createMavenInstallFile(vararg artifacts: Pair<String, String>) {
    val artifactEntries = artifacts.joinToString(separator = ",\n") { (coordinate, version) ->
      """
      |    "$coordinate": {
      |      "shasums": {
      |        "jar": "fakesha"
      |      },
      |      "version": "$version"
      |    }
      """.trimMargin()
    }
    File(repoDir, "third_party/maven_install.json").writeText(
      """
      |{
      |  "artifacts": {
      |$artifactEntries
      |  },
      |  "repositories": {
      |    "https://maven.google.com/": []
      |  }
      |}
      """.trimMargin()
    )
  }

  @Test
  fun testCheck_coordinateAbsentFromMaven_fails() {
    createVersionsFile(httpDependency("dagger", "2.41", "com.google.dagger:dager"))
    createMavenInstallFile("com.google.dagger:dagger" to "2.41")

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    assertThat(outContent.toString()).contains(
      "dagger declares com.google.dagger:dager, which Maven does not resolve."
    )
  }

  private fun httpDependency(
    name: String,
    version: String,
    mavenCoordinate: String?
  ): Triple<String, String, String?> = Triple(name, version, mavenCoordinate)

  private fun createExemption(
    dependencyName: String,
    reason: String
  ): DuplicateDependencyVersionExemption {
    return DuplicateDependencyVersionExemption.newBuilder().apply {
      exemptedDependencyName = dependencyName
      this.reason = reason
    }.build()
  }

  private fun runScript(vararg exemptions: DuplicateDependencyVersionExemption): Boolean {
    val exemptionsFile = File(tempFolder.root, "duplicate_dependency_version_exemptions.pb")
    DuplicateDependencyVersionExemptions.newBuilder().apply {
      addAllDuplicateDependencyVersionExemption(exemptions.toList())
    }.build().writeTo(exemptionsFile.outputStream())

    return DuplicateDependencyVersionCheck(
      repoPath = repoDir.absolutePath + "/",
      exemptionProtoPath = exemptionsFile.absolutePath
    ).execute()
  }
}
