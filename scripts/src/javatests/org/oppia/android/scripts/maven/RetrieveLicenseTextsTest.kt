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
import org.oppia.android.scripts.license.LicenseFetcher
import org.oppia.android.scripts.proto.DirectLinkOnly
import org.oppia.android.scripts.proto.ExtractedCopyLink
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.ScrapableLink
import org.oppia.android.testing.assertThrows
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.random.Random

/** Tests for [RetrieveLicenseTexts]. */
class RetrieveLicenseTextsTest {
  private val TOO_FEW_ARGS_FAILURE = "Too few arguments passed"
  private val MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE =
    "maven_dependencies.textproto is not up-to-date"
  private val SCRIPT_PASSED_INDICATOR = "Script execution completed successfully."
  private val LONG_LICENSE_TEXT_LENGTH = MAX_LICENSE_LENGTH + 1

  private val SCRAPABLE_LINK = "https://www.apache.org/licenses/LICENSE-2.0.txt"
  private val DIRECT_LINK_ONLY = "https://developer.android.com/studio/terms.html"
  private val EXTRACTED_COPY_ORIGINAL_LINK = "https://www.opensource.org/licenses/bsd-license"
  private val EXTRACTED_COPY_LINK = "https://raw.githubusercontent.com/oppia/oppia-android-" +
    "licenses/develop/simplified-bsd-license.txt"
  private val LONG_LICENSE_TEXT_LINK = "https://verylonglicense.txt"

  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("values")
    tempFolder.newFolder("scripts", "assets")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testScript_oneArgument_printsUsageStringAndThrowsException() {
    val exception = assertThrows(Exception::class) {
      RetrieveLicenseTexts(mockLicenseFetcher).main(arrayOf())
    }

    assertThat(exception).hasMessageThat().contains(TOO_FEW_ARGS_FAILURE)
    assertThat(outContent.toString()).contains("Usage:")
  }

  @Test
  fun testScript_oneArguments_printsUsageStringAndThrowsException() {
    val exception = assertThrows(Exception::class) {
      RetrieveLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values"
        )
      )
    }

    assertThat(exception).hasMessageThat().contains(TOO_FEW_ARGS_FAILURE)
    assertThat(outContent.toString()).contains("Usage:")
  }

  @Test
  fun testScript_dependencyListEmpty_failsWithNotUpToDateException() {
    val mavenDependencyList = createMavenDependencyList()

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val exception = assertThrows(Exception::class) {
      RetrieveLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb",
          "${tempFolder.root}/large_strings.textproto"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_licenseListEmpty_failsWithNotUpToDateException() {
    val dependencyList = listOf<MavenDependency>(createMavenDependency("artifact:name"))
    val mavenDependencyList = createMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val exception = assertThrows(Exception::class) {
      RetrieveLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb",
          "${tempFolder.root}/large_strings.textproto"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_verifiedLinkNotSet_failsWithNotUpToDateException() {
    val licenseList = listOf<License>(
      createLicenseWithVerifiedLinkNotSet(
        licenseName = "Apache License",
        originalLink = SCRAPABLE_LINK
      )
    )
    val dependencyList = listOf<MavenDependency>(
      createMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = createMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    val exception = assertThrows(Exception::class) {
      RetrieveLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb",
          "${tempFolder.root}/large_strings.textproto"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_validDependencyList_executesSuccessfully() {
    val scrapableLinkLicense = createLicenseWithScrapableLink(
      licenseName = "Apache License",
      originalLink = SCRAPABLE_LINK,
      scrapableLinkUrl = SCRAPABLE_LINK
    )
    val directLinkOnlyLicense = createLicenseWithDirectLinkOnlyLink(
      licenseName = "Android Terms of Service",
      originalLink = DIRECT_LINK_ONLY,
      directLinkOnlyUrl = DIRECT_LINK_ONLY
    )
    val dependencyList = listOf<MavenDependency>(
      createMavenDependency(
        artifactName = "artifact.name:A:1",
        artifactVersion = "2.0.1",
        licenseList = listOf(
          scrapableLinkLicense
        )
      ),
      createMavenDependency(
        artifactName = "artifact.name:B:1",
        artifactVersion = "2.3.1",
        licenseList = listOf(
          directLinkOnlyLicense
        )
      )
    )
    val mavenDependencyList = createMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    RetrieveLicenseTexts(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}/values",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb",
        "${tempFolder.root}/large_strings.textproto"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_INDICATOR)
  }

  @Test
  fun testScript_validDependencyList_generatesResourceXmlFile() {
    val scrapableLinkLicense = createLicenseWithScrapableLink(
      licenseName = "Apache License",
      originalLink = SCRAPABLE_LINK,
      scrapableLinkUrl = SCRAPABLE_LINK
    )
    val directLinkOnlyLicense = createLicenseWithDirectLinkOnlyLink(
      licenseName = "Android Terms of Service",
      originalLink = DIRECT_LINK_ONLY,
      directLinkOnlyUrl = DIRECT_LINK_ONLY
    )
    val extractedCopyLinkLicense = createLicenseWithExtractedCopyLink(
      licenseName = "BSD License",
      originalLink = EXTRACTED_COPY_ORIGINAL_LINK,
      extractedCopyLinkUrl = EXTRACTED_COPY_LINK
    )
    val longTextLicense = createLicenseWithScrapableLink(
      licenseName = "Long License Text",
      originalLink = LONG_LICENSE_TEXT_LINK,
      scrapableLinkUrl = LONG_LICENSE_TEXT_LINK
    )
    val dependencyList = listOf<MavenDependency>(
      createMavenDependency(
        artifactName = "artifact.name:A:2.0.1",
        artifactVersion = "2.0.1",
        licenseList = listOf(scrapableLinkLicense, longTextLicense)
      ),
      createMavenDependency(
        artifactName = "artifact.name:B:4.1.1",
        artifactVersion = "4.1.1",
        licenseList = listOf(directLinkOnlyLicense, extractedCopyLinkLicense)
      ),
      createMavenDependency(
        artifactName = "artifact.name:C:3.1.1",
        artifactVersion = "3.1.1",
        licenseList = listOf(scrapableLinkLicense)
      )
    )
    val mavenDependencyList = createMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val xmlFile = tempFolder.newFile("values/third_party_dependencies.xml")
    pbFile.outputStream().use { mavenDependencyList.writeTo(it) }

    RetrieveLicenseTexts(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}/values",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb",
        "${tempFolder.root}/large_strings.textproto"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_INDICATOR)
    val xmlContent = xmlFile.inputStream().bufferedReader().use { it.readText() }
    val dependencyNamesList = retrieveListOfStrings(xmlContent, "third_party_dependency_name_")
    val dependencyVersionsList =
      retrieveListOfStrings(xmlContent, "third_party_dependency_version_")
    val licenseTextsList = retrieveListOfStrings(xmlContent, "license_text_")
    val licenseNamesList = retrieveListOfStrings(xmlContent, "license_name_")

    assertThat(dependencyNamesList).containsExactly(
      "artifact.name:A",
      "artifact.name:B",
      "artifact.name:C"
    )

    assertThat(dependencyVersionsList).containsExactly(
      "2.0.1",
      "4.1.1",
      "3.1.1"
    )

    assertThat(licenseTextsList).containsExactly(
      """
      "                        Apache License
      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document."
      """.trimIndent(),
      "\"$LONG_LICENSE_TEXT_LINK\"",
      "\"$DIRECT_LINK_ONLY\"",
      """
      "Copyright <YEAR> <COPYRIGHT HOLDER>

      Redistribution and use in source and binary forms, with or without modification, are 
      permitted provided that the following conditions are met:"
      """.trimIndent()
    )

    assertThat(licenseNamesList).containsExactly(
      "Apache License",
      "Long License Text",
      "Android Terms of Service",
      "BSD License"
    )

    val dependencyNamesArray = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_names_array",
      arrayTag = "string-array"
    )
    val dependencyVersionsArray = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_versions_array",
      arrayTag = "string-array"
    )
    val licenseTextsArray0 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_texts_0",
      arrayTag = "string-array"
    )
    val licenseTextsArray1 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_texts_1",
      arrayTag = "string-array"
    )
    val licenseTextsArray2 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_texts_2",
      arrayTag = "string-array"
    )
    val licenseNamesArray0 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_names_0",
      arrayTag = "string-array"
    )
    val licenseNamesArray1 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_names_1",
      arrayTag = "string-array"
    )
    val licenseNamesArray2 = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_names_2",
      arrayTag = "string-array"
    )
    val arrayOfLicenseNamesArrays = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_names_array",
      arrayTag = "array"
    )
    val arrayOfLicenseTextsArrays = retrieveArray(
      xmlContent = xmlContent,
      attributeName = "third_party_dependency_license_texts_array",
      arrayTag = "array"
    )
    verifyArray(
      itemList = dependencyNamesArray,
      indicesList = listOf(0, 1, 2),
      expectedSize = 3,
      prefix = "@string/third_party_dependency_name_"
    )
    verifyArray(
      itemList = dependencyVersionsArray,
      indicesList = listOf(0, 1, 2),
      expectedSize = 3,
      prefix = "@string/third_party_dependency_version_"
    )
    verifyArray(
      itemList = licenseTextsArray0,
      indicesList = listOf(0, 1),
      expectedSize = 2,
      prefix = "@string/license_text_"
    )
    verifyArray(
      itemList = licenseTextsArray1,
      indicesList = listOf(2, 3),
      expectedSize = 2,
      prefix = "@string/license_text_"
    )
    verifyArray(
      itemList = licenseTextsArray2,
      indicesList = listOf(0),
      expectedSize = 1,
      prefix = "@string/license_text_"
    )
    verifyArray(
      itemList = licenseNamesArray0,
      indicesList = listOf(0, 1),
      expectedSize = 2,
      prefix = "@string/license_name_"
    )
    verifyArray(
      itemList = licenseNamesArray1,
      indicesList = listOf(2, 3),
      expectedSize = 2,
      prefix = "@string/license_name_"
    )
    verifyArray(
      itemList = licenseNamesArray2,
      indicesList = listOf(0),
      expectedSize = 1,
      prefix = "@string/license_name_"
    )
    verifyArray(
      itemList = arrayOfLicenseNamesArrays,
      indicesList = List(dependencyNamesList.size) { it },
      expectedSize = dependencyNamesList.size,
      prefix = "@array/third_party_dependency_license_names_"
    )
    verifyArray(
      itemList = arrayOfLicenseTextsArrays,
      indicesList = List(dependencyList.size) { it },
      expectedSize = dependencyList.size,
      prefix = "@array/third_party_dependency_license_texts_"
    )
  }

  /**
   * Helper function to verify the content of the array of the resource elements in
   * third_party_dependencies.xml.
   *
   * @param itemList list of values obtained by parsing the array
   * @param prefix the prefix with which each item of the array should start with
   * @param indicesList list of indices that should match be present after the prefix in the item
   * @param expectedSize the expected size of the array
   */
  private fun verifyArray(
    itemList: List<String>,
    prefix: String,
    indicesList: List<Int>,
    expectedSize: Int,
  ) {
    assertThat(itemList.size).isEqualTo(expectedSize)
    itemList.forEachIndexed { index, item ->
      val expectedIndex = indicesList[index]
      assertThat(item).isEqualTo("$prefix$expectedIndex")
    }
  }

  /** Returns an instance of MavenDependency proto message. */
  private fun createMavenDependency(
    artifactName: String,
    artifactVersion: String = "1.0.0",
    licenseList: List<License> = listOf<License>()
  ): MavenDependency {
    return MavenDependency.newBuilder().apply {
      this.artifactName = artifactName
      this.artifactVersion = artifactVersion
      this.addAllLicense(licenseList)
    }.build()
  }

  /** Returns an instance of MavenDependencyList proto message. */
  private fun createMavenDependencyList(
    dependenciesList: List<MavenDependency> = listOf<MavenDependency>()
  ): MavenDependencyList {
    return MavenDependencyList
      .newBuilder()
      .addAllMavenDependency(dependenciesList)
      .build()
  }

  /** Returns a License that has verifiedLink not set. */
  private fun createLicenseWithVerifiedLinkNotSet(
    licenseName: String,
    originalLink: String,
  ): License {
    return License.newBuilder().apply {
      this.licenseName = licenseName
      this.originalLink = originalLink
    }.build()
  }

  /** Returns a License with ScrapableLink. */
  private fun createLicenseWithScrapableLink(
    licenseName: String,
    originalLink: String,
    scrapableLinkUrl: String
  ): License {
    return License.newBuilder().apply {
      this.licenseName = licenseName
      this.originalLink = originalLink
      this.scrapableLink = ScrapableLink.newBuilder().setUrl(scrapableLinkUrl).build()
    }.build()
  }

  /** Returns a License with ExtractedCopyLink. */
  private fun createLicenseWithExtractedCopyLink(
    licenseName: String,
    originalLink: String,
    extractedCopyLinkUrl: String
  ): License {
    return License.newBuilder().apply {
      this.licenseName = licenseName
      this.originalLink = originalLink
      this.extractedCopyLink = ExtractedCopyLink.newBuilder().setUrl(extractedCopyLinkUrl).build()
    }.build()
  }

  /** Returns a License with DirectOnlyLink. */
  private fun createLicenseWithDirectLinkOnlyLink(
    licenseName: String,
    originalLink: String,
    directLinkOnlyUrl: String
  ): License {
    return License.newBuilder().apply {
      this.licenseName = licenseName
      this.originalLink = originalLink
      this.directLinkOnly = DirectLinkOnly.newBuilder().setUrl(directLinkOnlyUrl).build()
    }.build()
  }

  /**
   * Parses the XML to return a list of values enclosed between string tags.
   *
   * @param xmlContent XML content to be parsed
   * @param attributePrefix the prefix of the attribute of <string> tag that needs to be parsed
   * @return list of parsed values of <string> nodes with the given attributrPrefix
   */
  private fun retrieveListOfStrings(
    xmlContent: String,
    attributePrefix: String
  ): List<String> {
    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(InputSource(xmlContent.byteInputStream()))

    val stringResources = doc.getElementsByTagName("string")
    val itemList = mutableListOf<String>()
    for (i in 0 until stringResources.getLength()) {
      if (stringResources.item(0).getNodeType() == Node.ELEMENT_NODE) {
        val element = stringResources.item(i) as Element
        if (element.getAttribute("name").toString().startsWith(attributePrefix)) {
          itemList.add(element.firstChild.nodeValue)
        }
      }
    }
    return itemList
  }

  private fun retrieveArray(
    xmlContent: String,
    attributeName: String,
    arrayTag: String
  ): List<String> {
    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(InputSource(xmlContent.byteInputStream()))

    val stringArrays = doc.getElementsByTagName(arrayTag)
    val itemList = mutableListOf<String>()
    for (i in 0 until stringArrays.getLength()) {
      if (stringArrays.item(0).getNodeType() == Node.ELEMENT_NODE) {
        val element = stringArrays.item(i) as Element
        if (element.getAttribute("name").toString() == attributeName) {
          val itemElements = element.getElementsByTagName("item")
          for (j in 0 until itemElements.length) {
            val itemElement = itemElements.item(j) as Element
            itemList.add(itemElement.firstChild.nodeValue)
          }
        }
      }
    }
    return itemList
  }

  private fun retrieveLongLicenseText(): String {
    val charPool: List<Char> = (' '..'z').toList() // Inlcude all chars from ASCII value 32 to 122.
    return (1..LONG_LICENSE_TEXT_LENGTH)
      .map { Random.nextInt(0, charPool.size) }
      .map(charPool::get)
      .joinToString("")
  }

  /** Returns a mock for the [LicenseFetcher]. */
  private fun initializeLicenseFetcher(): LicenseFetcher {
    return mock<LicenseFetcher> {
      on { scrapeText(eq(SCRAPABLE_LINK)) }
        .doReturn(
          """
                                  Apache License
          "License" shall mean the terms and conditions for use, reproduction,
          and distribution as defined by Sections 1 through 9 of this document.
          """.trimIndent()
        )
      on { scrapeText(eq(DIRECT_LINK_ONLY)) }
        .doReturn(DIRECT_LINK_ONLY)
      on { scrapeText(eq(LONG_LICENSE_TEXT_LINK)) }
        .doReturn(retrieveLongLicenseText())
      on { scrapeText(eq(EXTRACTED_COPY_LINK)) }
        .doReturn(
          """
          Copyright <YEAR> <COPYRIGHT HOLDER>

          Redistribution and use in source and binary forms, with or without modification, are 
          permitted provided that the following conditions are met:
          """.trimIndent()
        )
    }
  }
}
