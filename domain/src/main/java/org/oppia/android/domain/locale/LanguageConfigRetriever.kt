package org.oppia.android.domain.locale

import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/**
 * Retriever for language configurations from the app's embedded assets.
 *
 * Note that this implementation is expected to no-op on Gradle builds since they don't include the
 * necessary configuration files. The rest of the locale & translation systems are expected to
 * gracefully fail in this case.
 */
class LanguageConfigRetriever @Inject constructor(private val assetRepository: AssetRepository) {
  /** Returns the [SupportedLanguages] configuration for the app, or default instance if none. */
  fun loadSupportedLanguages(): SupportedLanguages {
    val supportedLanguagesData = SupportedLanguages.newBuilder()

    for (x in 0..4) {
      supportedLanguagesData.apply {
        addLanguageDefinitions(getDummyLanguageDefinition(x))
      }.build()
    }

    return supportedLanguagesData.build()
  }

  /** Returns the [SupportedRegions] configuration for the app, or default instance if none. */
  fun loadSupportedRegions(): SupportedRegions {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_regions", SupportedRegions.getDefaultInstance()
    )
  }

  /** Returns dummy [LanguageSupportDefinition] for building [SupportedLanguages] object */
  private fun getDummyLanguageDefinition(languageNamePosition: Int): LanguageSupportDefinition {
    return LanguageSupportDefinition.newBuilder().apply {
      language = getDummyLanguageName(languageNamePosition)
      minAndroidSdkVersion = 1
      appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
        ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
          ietfLanguageTag = getDummyLanguageTag(languageNamePosition)
        }.build()
        androidResourcesLanguageId =
          LanguageSupportDefinition.AndroidLanguageId.newBuilder().apply {
            languageCode = getDummyLanguageTag(languageNamePosition)
          }.build()
      }.build()
      audioTranslationId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
        ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
          ietfLanguageTag = getDummyLanguageTag(languageNamePosition)
        }.build()
      }.build()
      contentStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
        ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
          ietfLanguageTag = getDummyLanguageTag(languageNamePosition)
        }.build()
        minAndroidSdkVersion = 1
      }.build()
      languageValue = getDummyLanguageValue(languageNamePosition)
    }.build()
  }

  /** Returns dummy [OppiaLanguage] for building [LanguageSupportDefinition] object */
  private fun getDummyLanguageName(languageNamePosition: Int): OppiaLanguage {
    val languageList = listOf(
      OppiaLanguage.ARABIC,
      OppiaLanguage.SWAHILI, OppiaLanguage.HINDI,
      OppiaLanguage.PORTUGUESE, OppiaLanguage.ENGLISH
    )
    return languageList[languageNamePosition]
  }

  /** Returns dummy [OppiaLanguage.value] for building [LanguageSupportDefinition] object */
  private fun getDummyLanguageValue(languageValuePosition: Int): Int {
    var dummyLanguageListValue = listOf(1, 7, 3, 5, 2)
    return dummyLanguageListValue[languageValuePosition]
  }

  /** Returns dummy OppiaLanguage tags for building [LanguageSupportDefinition] object */
  private fun getDummyLanguageTag(languageValuePosition: Int): String {
    val dummyLanguageTagList = listOf("ar", "sw", "hi", "pt", "en")
    return dummyLanguageTagList[languageValuePosition]
  }
}
