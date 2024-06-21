package org.oppia.android.scripts.gae.proto

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.oppia.proto.v1.structure.LanguageType
import java.net.URL
import java.util.Locale

class OppiaWebTranslationExtractor private constructor(
  private val webTranslationMapping: Map<LanguageType, Map<String, String>>
) {
  fun retrieveTranslations(
    id: TranslatableActivityId,
    requestedContentId: String
  ): Map<LanguageType, String> {
    return SUPPORTED_LANGUAGES.mapNotNull { language ->
      val languageTranslations = webTranslationMapping.getValue(language)
      languageTranslations[id.computeWebKeyForContent(requestedContentId)]?.let { language to it }
    }.toMap()
  }

  sealed class TranslatableActivityId(private val activityType: String) {
    private val upperCasedActivityType by lazy { activityType.uppercase(Locale.US) }

    abstract val activityId: String

    internal fun computeWebKeyForContent(contentId: String): String =
      "I18N_${upperCasedActivityType}_${activityId}_${contentId.uppercase(Locale.US)}"

    // TODO: Figure out if this should be ID or URL fragment (or if there's even web translations for these).
    data class Classroom(val classroomId: String)  : TranslatableActivityId(activityType = "classroom") {
      override val activityId: String = classroomId
    }

    data class Topic(val topicId: String) : TranslatableActivityId(activityType = "topic") {
      override val activityId: String = topicId
    }

    data class Subtopic(
      val topicId: String,
      val subtopicWebUrlFragment: String
    ) : TranslatableActivityId(activityType = "subtopic") {
      override val activityId: String = "${topicId}_$subtopicWebUrlFragment"
    }

    data class Story(val storyId: String) : TranslatableActivityId(activityType = "story") {
      override val activityId: String = storyId
    }

    // TODO: Document that this is technically not supported yet.
    data class Skill(val skillId: String) : TranslatableActivityId(activityType = "skill") {
      override val activityId: String = skillId
    }

    data class Exploration(
      val explorationId: String
    ) : TranslatableActivityId(activityType = "exploration") {
      override val activityId: String = explorationId
    }
  }

  companion object {
    private const val OPPIA_WEB_GITHUB_BASE_URL = "https://raw.githubusercontent.com/oppia/oppia"
    private const val REFERENCED_WEB_BLOB = "develop"
    private const val RELATIVE_TRANSLATIONS_ASSETS_DIR = "assets/i18n"
    private const val OPPIA_WEB_ASSETS_COMPLETE_URL =
      "$OPPIA_WEB_GITHUB_BASE_URL/$REFERENCED_WEB_BLOB/$RELATIVE_TRANSLATIONS_ASSETS_DIR"
    private val SUPPORTED_LANGUAGES = LanguageType.values().filter {
      it != LanguageType.LANGUAGE_CODE_UNSPECIFIED && it != LanguageType.UNRECOGNIZED
    }

    suspend fun createExtractor(): OppiaWebTranslationExtractor =
      OppiaWebTranslationExtractor(downloadTranslationMaps())

    private suspend fun downloadTranslationMaps(): Map<LanguageType, Map<String, String>> =
      SUPPORTED_LANGUAGES.map(::downloadTranslationMapAsync).awaitAll().toMap()

    private fun downloadTranslationMapAsync(
      language: LanguageType
    ): Deferred<Pair<LanguageType, Map<String, String>>> {
      return CoroutineScope(Dispatchers.IO).async {
        val languageCode = language.toOppiaWebAssetsLanguageCode()
        val languageJsonUrl = URL("$OPPIA_WEB_ASSETS_COMPLETE_URL/$languageCode.json")
        val languageJson = languageJsonUrl.downloadTextContents()
        val jsonMapAdapter = Moshi.Builder().build().adapter(Map::class.java)
        return@async language to (jsonMapAdapter.fromJson(languageJson)?.safeCast() ?: mapOf())
      }
    }

    private fun URL.downloadTextContents() = openStream().bufferedReader().use { it.readText() }

    private inline fun <reified K, reified V> Map<*, *>.safeCast(): Map<K, V> {
      check(keys.all { it is K })
      check(values.all { it is V })
      @Suppress("UNCHECKED_CAST") // Safe since the types are checked above.
      return this as Map<K, V>
    }

    private fun LanguageType.toOppiaWebAssetsLanguageCode(): String {
      return when (this) {
        LanguageType.ENGLISH -> "en"
        LanguageType.ARABIC -> "ar"
        LanguageType.HINDI, LanguageType.HINGLISH -> "hi" // No Hinglish-specific translations.
        LanguageType.BRAZILIAN_PORTUGUESE -> "pt-br"
        LanguageType.SWAHILI -> "sw"
        LanguageType.NIGERIAN_PIDGIN -> "en" // No Naija-specific translations.
        LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED ->
          error("Language is not available in Oppia web's frontend localization strings: $this.")
      }
    }
  }
}
