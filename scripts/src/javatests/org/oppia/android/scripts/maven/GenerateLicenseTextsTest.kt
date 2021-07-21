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

/** Tests for [GenerateLicenseTexts]. */
class GenerateLicenseTextsTest {
  private val TOO_FEW_ARGS_FAILURE = "Too few arguments passed"
  private val MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE =
    "maven_dependencies.textproto is not up-to-date"
  private val SCRIPT_PASSED_INDICATOR = "Script execution completed successfully."

  private val SCRAPABLE_LINK = "https://www.apache.org/licenses/LICENSE-2.0.txt"
  private val DIRECT_LINK_ONLY = "https://developer.android.com/studio/terms.html"
  private val EXTRACTED_COPY_ORIGINAL_LINK = "https://www.opensource.org/licenses/bsd-license"
  private val EXTRACTED_COPY_LINK = "https://raw.githubusercontent.com/oppia/oppia-android-" +
    "licenses/develop/simplified-bsd-license.txt"

  private val VALID_LINK = "https://www.apache.org/licenses/LICENSE-2.0.txt"
  private val INVALID_LINK = "https://fabric.io/terms"

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val mockLicenseFetcher by lazy { initializeLicenseFetcher() }

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
  fun testScript_noArguments_printsUsageStringAndThrowsException() {
    val exception = assertThrows(Exception::class) {
      GenerateLicenseTexts(mockLicenseFetcher).main(arrayOf())
    }

    assertThat(exception).hasMessageThat().contains(TOO_FEW_ARGS_FAILURE)
    assertThat(outContent.toString()).contains("Usage:")
  }

  @Test
  fun testScript_oneArguments_printsUsageStringAndThrowsException() {
    val exception = assertThrows(Exception::class) {
      GenerateLicenseTexts(mockLicenseFetcher).main(
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
    val mavenDependencyList = getMavenDependencyList()

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) {
      GenerateLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_licenseListEmpty_failsWithNotUpToDateException() {
    val dependencyList = listOf<MavenDependency>(getMavenDependency("artifact:name"))
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) {
      GenerateLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_verifiedLinkNotSet_failsWithNotUpToDateException() {
    val licenseList = listOf<License>(
      getLicenseWithVerifiedLinkNotSet(
        licenseName = "Apache License",
        originalLink = SCRAPABLE_LINK
      )
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact:name",
        licenseList = licenseList
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    val exception = assertThrows(Exception::class) {
      GenerateLicenseTexts(mockLicenseFetcher).main(
        arrayOf(
          "${tempFolder.root}/values",
          "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
        )
      )
    }
    assertThat(exception).hasMessageThat().contains(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE_FAILURE)
  }

  @Test
  fun testScript_validDependencyList_executesSuccessfully() {
    val scrapableLinkLicense = getLicenseWithScrapableLink(
      licenseName = "Apache License",
      originalLink = SCRAPABLE_LINK,
      scrapableLinkUrl = SCRAPABLE_LINK
    )
    val directLinkOnlyLicense = getLicenseWithDirectLinkOnlyLink(
      licenseName = "Android Terms of Service",
      originalLink = DIRECT_LINK_ONLY,
      directLinkOnlyUrl = DIRECT_LINK_ONLY
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact.name:1",
        artifactVersion = "2.0.1",
        licenseList = listOf(
          scrapableLinkLicense
        )
      ),
      getMavenDependency(
        artifactName = "artifact.name:2",
        artifactVersion = "2.3.1",
        licenseList = listOf(
          directLinkOnlyLicense
        )
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    mavenDependencyList.writeTo(pbFile.outputStream())

    GenerateLicenseTexts(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}/values",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_INDICATOR)
  }

  @Test
  fun testScript_validDependencyList_generatesResourceXmlFiles() {
    val scrapableLinkLicense = getLicenseWithScrapableLink(
      licenseName = "Apache License",
      originalLink = SCRAPABLE_LINK,
      scrapableLinkUrl = SCRAPABLE_LINK
    )
    val directLinkOnlyLicense = getLicenseWithDirectLinkOnlyLink(
      licenseName = "Android Terms of Service",
      originalLink = DIRECT_LINK_ONLY,
      directLinkOnlyUrl = DIRECT_LINK_ONLY
    )
    val extractedCopyLinkLicense = getLicenseWithExtractedCopyLink(
      licenseName = "BSD License",
      originalLink = EXTRACTED_COPY_ORIGINAL_LINK,
      extractedCopyLinkUrl = EXTRACTED_COPY_LINK
    )
    val dependencyList = listOf<MavenDependency>(
      getMavenDependency(
        artifactName = "artifact.name:1",
        artifactVersion = "2.0.1",
        licenseList = listOf(scrapableLinkLicense)
      ),
      getMavenDependency(
        artifactName = "artifact.name:2",
        artifactVersion = "4.1.1",
        licenseList = listOf(directLinkOnlyLicense, extractedCopyLinkLicense)
      ),
      getMavenDependency(
        artifactName = "artifact.name:3",
        artifactVersion = "3.1.1",
        licenseList = listOf(scrapableLinkLicense)
      )
    )
    val mavenDependencyList = getMavenDependencyList(dependencyList)

    val pbFile = tempFolder.newFile("scripts/assets/maven_dependencies.pb")
    val xmlFile = tempFolder.newFile("values/third_party_dependencies.xml")
    mavenDependencyList.writeTo(pbFile.outputStream())

    GenerateLicenseTexts(mockLicenseFetcher).main(
      arrayOf(
        "${tempFolder.root}/values",
        "${tempFolder.root}/scripts/assets/maven_dependencies.pb"
      )
    )
    assertThat(outContent.toString()).contains(SCRIPT_PASSED_INDICATOR)
    val xmlContent = xmlFile.inputStream().bufferedReader().use { it.readText() }
    val dependencyNamesList = retreiveListOfStrings(xmlContent, "third_party_dependency_name_")
    val dependencyVersionsList =
      retreiveListOfStrings(xmlContent, "third_party_dependency_version_")
    val licenseTextsList = retreiveListOfStrings(xmlContent, "license_text_")
    val licenseNamesList = retreiveListOfStrings(xmlContent, "license_name_")

    verifyList(
      itemList = dependencyNamesList,
      expectedSize = 3,
      expectedList = listOf(
        "artifact.name:1",
        "artifact.name:2",
        "artifact.name:3"
      )
    )
    verifyList(
      itemList = dependencyVersionsList,
      expectedSize = 3,
      expectedList = listOf(
        "2.0.1",
        "4.1.1",
        "3.1.1"
      )
    )
    verifyList(
      itemList = licenseTextsList,
      expectedSize = 3,
      expectedList = listOf(
        """
        <![CDATA[Apache License
        "License" shall mean the terms and conditions for use, reproduction,
        and distribution as defined by Sections 1 through 9 of this document.]]>
        """.trimIndent(),
        "<![CDATA[$DIRECT_LINK_ONLY]]>",
        """
        <![CDATA[Copyright &lt;YEAR&gt; &lt;COPYRIGHT HOLDER&gt;

        Redistribution and use in source and binary forms, with or without modification, are 
        permitted provided that the following conditions are met:]]>
        """.trimIndent()
      )
    )
    verifyList(
      itemList = licenseNamesList,
      expectedSize = 3,
      expectedList = listOf(
        "Apache License",
        "Android Terms of Service",
        "BSD License"
      )
    )

    val dependencyNamesArray = retrieveArrayList(
      xmlContent,
      "third_party_dependency_names_array",
      "string-array"
    )
    val dependencyVersionsArray = retrieveArrayList(
      xmlContent,
      "third_party_dependency_versions_array",
      "string-array"
    )
    val licenseTextsArray0 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_texts_0",
      "string-array"
    )
    val licenseTextsArray1 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_texts_1",
      "string-array"
    )
    val licenseTextsArray2 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_texts_2",
      "string-array"
    )
    val licenseNamesArray0 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_names_0",
      "string-array"
    )
    val licenseNamesArray1 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_names_1",
      "string-array"
    )
    val licenseNamesArray2 = retrieveArrayList(
      xmlContent,
      "third_party_dependency_license_names_2",
      "string-array"
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
      indicesList = listOf(0),
      expectedSize = 1,
      prefix = "@string/license_text_"
    )
    verifyArray(
      itemList = licenseTextsArray1,
      indicesList = listOf(1, 2),
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
      indicesList = listOf(0),
      expectedSize = 1,
      prefix = "@string/license_name_"
    )
    verifyArray(
      itemList = licenseNamesArray1,
      indicesList = listOf(1, 2),
      expectedSize = 2,
      prefix = "@string/license_name_"
    )
    verifyArray(
      itemList = licenseNamesArray2,
      indicesList = listOf(0),
      expectedSize = 1,
      prefix = "@string/license_name_"
    )
  }

  private fun verifyArray(
    itemList: List<String>,
    indicesList: List<Int>,
    expectedSize: Int,
    prefix: String
  ) {
    assertThat(itemList.size).isEqualTo(expectedSize)
    itemList.forEachIndexed { index, item ->
      val expectedIndex = indicesList[index]
      assertThat(item).isEqualTo("$prefix$expectedIndex")
    }
  }

//  private fun verifyArray(itemList: List<String>, expectedSize: Int, prefix: String) {
//    assertThat(itemList.size).isEqualTo(expectedSize)
//    itemList.forEachIndexed { index, item ->
//      assertThat(item).isEqualTo("$prefix$index")
//    }
//  }

  private fun verifyList(itemList: List<String>, expectedSize: Int, expectedList: List<String>) {
    assertThat(itemList.size).isEqualTo(expectedSize)
    itemList.forEachIndexed { index, item ->
      assertThat(item).isEqualTo(expectedList[index])
    }
  }

  private fun getMavenDependency(
    artifactName: String,
    artifactVersion: String = "1.0.0",
    licenseList: List<License> = listOf<License>()
  ): MavenDependency {
    return MavenDependency
      .newBuilder()
      .setArtifactName(artifactName)
      .setArtifactVersion(artifactVersion)
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

  private fun getLicenseWithVerifiedLinkNotSet(
    licenseName: String,
    originalLink: String,
  ): License {
    return License.newBuilder().apply {
      this.licenseName = licenseName
      this.originalLink = originalLink
    }.build()
  }

  private fun getLicenseWithScrapableLink(
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

  private fun getLicenseWithExtractedCopyLink(
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

  private fun getLicenseWithDirectLinkOnlyLink(
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

  private fun retreiveListOfStrings(
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

  private fun retrieveArrayList(
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
      on { scrapeText(eq(EXTRACTED_COPY_LINK)) }
        .doReturn(
          """
          Copyright &lt;YEAR&gt; &lt;COPYRIGHT HOLDER&gt;

          Redistribution and use in source and binary forms, with or without modification, are 
          permitted provided that the following conditions are met:
          """.trimIndent()
        )
    }
  }
}
