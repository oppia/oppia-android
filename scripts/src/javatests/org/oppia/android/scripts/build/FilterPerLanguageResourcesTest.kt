package org.oppia.android.scripts.build

import com.android.aapt.ConfigurationOuterClass.Configuration
import com.android.aapt.Resources
import com.android.aapt.Resources.ConfigValue
import com.android.aapt.Resources.Entry
import com.android.aapt.Resources.Item
import com.android.aapt.Resources.Package
import com.android.aapt.Resources.ResourceTable
import com.android.aapt.Resources.Type
import com.android.aapt.Resources.Value
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.AndroidLanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.scripts.build.FilterPerLanguageResourcesTest.Resource.ColorResource
import org.oppia.android.scripts.build.FilterPerLanguageResourcesTest.Resource.StringResource
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** Tests for the filter_per_language_resources utility. */
// PrivatePropertyName: it's valid to have private vals in constant case if they're true constants.
// FunctionName: test names are conventionally named with underscores.
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
@Suppress("PrivatePropertyName", "FunctionName", "SameParameterValue")
class FilterPerLanguageResourcesTest {
  private val USAGE_STRING =
    "Usage: bazel run //scripts:filter_per_language_resources --" +
      " </absolute/path/to/input_module.zip:Path>" +
      " </absolute/path/to/output_module.zip:Path>"

  private val STR_RESOURCE_0_EN = StringResource(mapOf("" to "en str0"))
  private val STR_RESOURCE_1_EN_PT = StringResource(mapOf("" to "en str1", "pt-BR" to "pt str1"))
  private val STR_RESOURCE_2_EN_SW = StringResource(mapOf("" to "en str2", "sw" to "sw str2"))
  private val COLOR_RESOURCE_0_EN_PT = ColorResource(mapOf("" to "0xDEF", "pt-BR" to "0xABC"))
  private val RESOURCE_TABLE_EN_PT_SW =
    createResourceTable(
      STR_RESOURCE_0_EN, STR_RESOURCE_1_EN_PT, STR_RESOURCE_2_EN_SW, COLOR_RESOURCE_0_EN_PT
    )

  private val ENGLISH =
    createLanguageSupportDefinition(language = OppiaLanguage.ENGLISH, languageCode = "en")
  private val BRAZILIAN_PORTUGUESE =
    createLanguageSupportDefinition(
      language = OppiaLanguage.BRAZILIAN_PORTUGUESE, languageCode = "pt", regionCode = "br"
    )
  private val SWAHILI =
    createLanguageSupportDefinition(language = OppiaLanguage.SWAHILI, languageCode = "sw")
  private val ARABIC =
    createLanguageSupportDefinition(language = OppiaLanguage.ARABIC, languageCode = "ar")
  private val SUPPORTED_LANGUAGES_EN = createSupportedLanguages(ENGLISH)
  private val SUPPORTED_LANGUAGES_EN_PT = createSupportedLanguages(ENGLISH, BRAZILIAN_PORTUGUESE)
  private val SUPPORTED_LANGUAGES_EN_PT_SW =
    createSupportedLanguages(ENGLISH, BRAZILIAN_PORTUGUESE, SWAHILI)
  private val SUPPORTED_LANGUAGES_EN_AR = createSupportedLanguages(ENGLISH, ARABIC)

  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testUtility_noArgs_failsWithUsageString() {
    val error = assertThrows(IllegalArgumentException::class) { runScript() }

    assertThat(error).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_oneArg_failsWithUsageString() {
    val error = assertThrows(IllegalArgumentException::class) { runScript("first_file.zip") }

    assertThat(error).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_threeArg_failsWithUsageString() {
    val error = assertThrows(IllegalArgumentException::class) {
      runScript(
        tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"), "extra_param"
      )
    }

    assertThat(error).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_resourceTableProtoMissingInZip_throwsFailure() {
    // Create an empty zip file.
    ZipOutputStream(File(tempFolder.root, "input.zip").outputStream()).close()

    val error = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))
    }

    assertThat(error).hasMessageThat().contains("Expected resources.pb in input zip file")
  }

  @Test
  fun testUtility_supportedLanguagesProtoMissingInZip_throwsFailure() {
    // Create a zip file with only resources.pb.
    ZipOutputStream(File(tempFolder.root, "input.zip").outputStream()).use { outputStream ->
      outputStream.putNextEntry(ZipEntry("resources.pb"))
      ResourceTable.getDefaultInstance().writeTo(outputStream)
    }

    val error = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))
    }

    assertThat(error)
      .hasMessageThat()
      .contains("Expected assets/supported_languages.pb in input zip file")
  }

  @Test
  fun testUtility_resourceTableIncludesNonOppiaResources_throwsFailure() {
    // Create a resource table with another app's resources.
    createZipWith(
      fileName = "input.zip",
      resourceTable = createResourceTable(packageName = "some.other.app"),
      supportedLanguages = SUPPORTED_LANGUAGES_EN
    )

    val error = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))
    }

    assertThat(error).hasMessageThat().contains("Expected Oppia package, not: some.other.app.")
  }

  @Test
  fun testUtility_resourceTable_noSupportedLanguages_keepsOnlyEnglish() {
    // "No supported languages" always implies English (since English must be supported).
    createZipWith(
      fileName = "input.zip",
      resourceTable = RESOURCE_TABLE_EN_PT_SW,
      supportedLanguages = SUPPORTED_LANGUAGES_EN
    )

    runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))

    // Only English should be kept.
    val presentLanguages = readSupportedResourceLanguagesFromZip(fileName = "output.zip")
    assertThat(presentLanguages).containsExactly("")
  }

  @Test
  fun testUtility_resourceTable_onlyPortugueseSupported_keepsOnlyEnglishAndPortuguese() {
    // "No supported languages" always implies English (since English must be supported).
    createZipWith(
      fileName = "input.zip",
      resourceTable = RESOURCE_TABLE_EN_PT_SW,
      supportedLanguages = SUPPORTED_LANGUAGES_EN_PT
    )

    runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))

    // Only English & Portuguese resources should be kept.
    val presentLanguages = readSupportedResourceLanguagesFromZip(fileName = "output.zip")
    assertThat(presentLanguages).containsExactly("", "pt-BR")
  }

  @Test
  fun testUtility_resourceTable_allSupportedLanguages_keepsEverything() {
    // "No supported languages" always implies English (since English must be supported).
    createZipWith(
      fileName = "input.zip",
      resourceTable = RESOURCE_TABLE_EN_PT_SW,
      supportedLanguages = SUPPORTED_LANGUAGES_EN_PT_SW
    )

    runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))

    // All resources should be kept.
    val presentLanguages = readSupportedResourceLanguagesFromZip(fileName = "output.zip")
    assertThat(presentLanguages).containsExactly("", "pt-BR", "sw")
  }

  @Test
  fun testUtility_resourceTable_supportedLanguageNotInTable_ignoresExtraLanguage() {
    // "No supported languages" always implies English (since English must be supported).
    createZipWith(
      fileName = "input.zip",
      resourceTable = RESOURCE_TABLE_EN_PT_SW,
      supportedLanguages = SUPPORTED_LANGUAGES_EN_AR
    )

    runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))

    // Arabic is ignored since there are no strings to be removed in the table.
    val presentLanguages = readSupportedResourceLanguagesFromZip(fileName = "output.zip")
    assertThat(presentLanguages).containsExactly("")
  }

  @Test
  fun testUtility_whenResourcesAreRemoved_printsDiagnosticInformation() {
    // "No supported languages" always implies English (since English must be supported).
    createZipWith(
      fileName = "input.zip",
      resourceTable = RESOURCE_TABLE_EN_PT_SW,
      supportedLanguages = SUPPORTED_LANGUAGES_EN
    )

    runScript(tempFolder.getFilePath("input.zip"), tempFolder.getFilePath("output.zip"))

    val outputLine = readStandardOutputLines().single()
    assertThat(outputLine)
      .isEqualTo(
        "2 resources are being removed that are tied to unsupported languages: [pt-BR, sw] (size" +
          " reduction: 73 bytes)."
      )
  }

  private fun createLanguageSupportDefinition(
    language: OppiaLanguage,
    languageCode: String,
    regionCode: String = ""
  ): LanguageSupportDefinition {
    return LanguageSupportDefinition.newBuilder().apply {
      this.language = language
      appStringId = LanguageId.newBuilder().apply {
        androidResourcesLanguageId = AndroidLanguageId.newBuilder().apply {
          this.languageCode = languageCode
          this.regionCode = regionCode
        }.build()
      }.build()
    }.build()
  }

  private fun createSupportedLanguages(
    vararg languageDefinitions: LanguageSupportDefinition
  ): SupportedLanguages {
    return SupportedLanguages.newBuilder().apply {
      addAllLanguageDefinitions(languageDefinitions.toList())
    }.build()
  }

  private fun createResourceTable(
    vararg resources: Resource,
    packageName: String = "org.oppia.android"
  ): ResourceTable {
    return ResourceTable.newBuilder().apply {
      addPackage(
        Package.newBuilder().apply {
          this.packageName = packageName
          addAllType(resources.map { it.toType() })
        }
      )
    }.build()
  }

  private fun Resource.toType(): Type {
    return Type.newBuilder().apply {
      name = when (this@toType) {
        is ColorResource -> "color"
        is StringResource -> "string"
      }

      addEntry(
        Entry.newBuilder().apply {
          addAllConfigValue(
            configurations.map { (languageCode, strValue) ->
              ConfigValue.newBuilder().apply {
                config = Configuration.newBuilder().apply {
                  locale = languageCode
                }.build()
                value = Value.newBuilder().apply {
                  item = Item.newBuilder().apply {
                    str = Resources.String.newBuilder().apply {
                      value = strValue
                    }.build()
                  }.build()
                }.build()
              }.build()
            }
          )
        }
      )
    }.build()
  }

  private fun createZipWith(
    fileName: String,
    resourceTable: ResourceTable,
    supportedLanguages: SupportedLanguages
  ) {
    val destZipFile = tempFolder.newFile(fileName)
    ZipOutputStream(destZipFile.outputStream()).use { outputStream ->
      outputStream.putNextEntry(ZipEntry("resources.pb"))
      resourceTable.writeTo(outputStream)

      outputStream.putNextEntry(ZipEntry("assets/supported_languages.pb"))
      supportedLanguages.writeTo(outputStream)
    }
  }

  private fun readSupportedResourceLanguagesFromZip(fileName: String): Set<String> {
    val resourceTable = ZipFile(File(tempFolder.root, fileName)).use { zipFile ->
      zipFile.getInputStream(zipFile.getEntry("resources.pb")).use { inputStream ->
        ResourceTable.parseFrom(inputStream)
      }
    }
    return resourceTable.packageList.flatMap { it.typeList }
      .flatMap { it.entryList }
      .flatMap { it.configValueList }
      .map { it.config.locale }
      .toSet()
  }

  private fun TemporaryFolder.getFilePath(fileName: String): String =
    File(root, fileName).absoluteFile.normalize().path

  private fun readStandardOutputLines(): List<String> =
    outContent.toByteArray().inputStream().bufferedReader().readLines()

  private fun runScript(vararg args: String) = main(*args)

  private sealed class Resource {
    abstract val configurations: Map<String, String>

    data class ColorResource(override val configurations: Map<String, String>) : Resource()

    data class StringResource(override val configurations: Map<String, String>) : Resource()
  }
}
