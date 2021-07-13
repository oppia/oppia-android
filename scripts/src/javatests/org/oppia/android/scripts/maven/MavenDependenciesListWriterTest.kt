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
import java.io.File
import java.io.PrintStream

/** Tests for [MavenDependenciesListWriter]. */
class MavenDependenciesListWriterTest {

//  val PLAIN_TEXT_SCRAPABLE_LICENSE_LINK = "https://www.apache.org/licenses/LICENSE-2.0.txt"
//  val PLAIN_TEXT_NON_SCRAPABLE_LICENSE_LINK = "https://opensource.org/licenses/MIT"
//  val NON_PLAIN_TEXT_LICENSE_LINK = "https://developer.android.com/studio/terms.html"
//  val INVALID_LICENSE_LINK = "https://www.fabric.io.terms"
//  val NO_LICENSE_LINK_AVAILABLE_IDENTIFIER = "NO_LICENSE_LINKS_AVAILABLE"

  private val LICENSE_DETAILS_INCOMPLETE_FAILURE = "License details are not completed."
  private val COMPLETE_LICENSE_DETAILS_MESSAGE = "Please complete all the details" +
    "for the following licenses:"

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("assets")
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
  fun testLicenseNeedManualWork_scriptFailsWithExceptionAndCallOut() {
    val dependencies = listOf<DependencyName>(
      DependencyName.DATA_BINDING,
      DependencyName.FIREBASE_ANALYTICS
    )
    val mavenDependencyList = getMavenDependencyList(dependencies)
    val mavenInstallJson = tempFolder.newFile("assets/test_maven_install.json")
    writeMavenInstallJson(mavenInstallJson)
    val pbFile = tempFolder.newFile("assets/test_maven_dependencies.pb")
    val textProtoFile = tempFolder.newFile("assets/test_maven_dependencies.textproto")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) {
      runScript(
        dependencies,
        arrayOf(
          "${tempFolder.root}",
          "assets/test_maven_install.json",
          "assets/test_maven_dependencies.pb"
        )
      )
    }

    assertThat(exception).hasMessageThat().contains(LICENSE_DETAILS_INCOMPLETE_FAILURE)

    runScript(
      dependencies,
      arrayOf(
        "${tempFolder.root}",
        "assets/test_maven_install.json",
        "assets/test_maven_dependencies.textproto",
        "${tempFolder.root}/assets/test_maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString().trim()).contains("Script executed successfully")
  }

  @Test
  fun dummy_test() {
    val num = 4
    assertThat(num).isEqualTo(4)
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

  private fun runScript(
    dependencyLicenseTypes: List<DependencyName> = listOf(
      DependencyName.DATA_BINDING,
      DependencyName.FIREBASE_ANALYTICS
    ),
    args: Array<String>
  ) {
    val pathToRoot = args[0]
    val pathToMavenInstall = "$pathToRoot/${args[1]}"
    val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"
    val pathToMavenDependenciesProtoBinary = args[3]

    val dependencyListsProvider = DependencyListsProvider(
      UtilityProviderInterceptor(
        dependencyLicenseTypes
      )
    )

    val bazelQueryDepsList = dependencyListsProvider.provideBazelQueryDependencyList(pathToRoot)
    val mavenInstallDepsList =
      dependencyListsProvider.getDependencyListFromMavenInstall(
        pathToMavenInstall,
        bazelQueryDepsList
      )

    val dependenciesListFromPom = dependencyListsProvider
      .provideDependencyListFromPom(mavenInstallDepsList)

    MavenDependenciesListWriter.pathToMavenDependenciesTextProto = pathToMavenDependenciesTextProto
    MavenDependenciesListWriter.pathToMavenDependenciesProtoBinary =
      pathToMavenDependenciesProtoBinary
    MavenDependenciesListWriter.dependenciesListFromPom =
      dependenciesListFromPom.mavenDependencyList

    MavenDependenciesListWriter.main(arrayOf())
  }

  private class UtilityProviderInterceptor(
    val dependencyNamesList: List<DependencyName>
  ) : UtilityProvider {
    override fun scrapeText(link: String): String {
      val DATA_BINDING_POM = "https://maven.google.com/androidx/databinding/" +
        "databinding-adapters/3.4.2/databinding-adapters-3.4.2.pom"
      val PROTO_LITE_POM = "https://repo1.maven.org/maven2/com/google/protobuf/" +
        "protobuf-lite/3.0.0/protobuf-lite-3.0.0.pom"
      val IO_FABRIC_POM = "https://maven.google.com/io/fabric/sdk/android/" +
        "fabric/1.4.7/fabric-1.4.7.pom"
      val GLIDE_ANNOTATIONS_POM = "https://repo1.maven.org/maven2/com/github/" +
        "bumptech/glide/annotations/4.11.0/annotations-4.11.0.pom"
      val FIREBASE_ANALYTICS_POM = "https://maven.google.com/com/google/firebase/" +
        "firebase-analytics/17.5.0/firebase-analytics-17.5.0.pom"
      return when (link) {
        DATA_BINDING_POM ->
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

        GLIDE_ANNOTATIONS_POM ->
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

        FIREBASE_ANALYTICS_POM ->
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

        IO_FABRIC_POM ->
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
        else ->
          """
          <?xml version="1.0" encoding="UTF-8"?>
          """.trimIndent()
      }
    }

    override fun retrieveThirdPartyMavenDependenciesList(
      rootPath: String,
      vararg args: String
    ): List<String> {
      val mavenPrefix = "@maven//:"
      val bazelQueryDepsNames = mutableListOf<String>()
      dependencyNamesList.forEach { dependencyName ->
        when (dependencyName) {
          DependencyName.DATA_BINDING ->
            bazelQueryDepsNames.add(mavenPrefix + "androidx_databinding_databinding_adapters")
          DependencyName.GLIDE_ANNOTATIONS ->
            bazelQueryDepsNames.add(mavenPrefix + "com_github_bumptech_glide_annotations")
          DependencyName.FIREBASE_ANALYTICS ->
            bazelQueryDepsNames.add(mavenPrefix + "com_google_firebase_firebase_analytics")
          DependencyName.PROTO_LITE ->
            bazelQueryDepsNames.add(mavenPrefix + "com_google_protobuf_protobuf_lite")
          else ->
            bazelQueryDepsNames.add(mavenPrefix + "io_fabric_sdk_android_fabric")
        }
      }
      return bazelQueryDepsNames.toList()
    }

  }
}
