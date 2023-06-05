package org.oppia.android.scripts.gae.proto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.awaitAll
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.json.GaeEntityTranslation
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.GaeRecordedVoiceovers
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeStoryNode
import org.oppia.android.scripts.gae.json.GaeSubtitledHtml
import org.oppia.android.scripts.gae.json.GaeSubtitledUnicode
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.json.GaeTranslatedContent
import org.oppia.android.scripts.gae.json.GaeVoiceover
import org.oppia.android.scripts.gae.json.GaeWrittenTranslation
import org.oppia.android.scripts.gae.json.GaeWrittenTranslations
import org.oppia.android.scripts.gae.json.SubtitledText
import org.oppia.android.scripts.gae.proto.OppiaWebTranslationExtractor.TranslatableActivityId
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.ContentLocalizationsDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.LocalizedConceptCardIdDto
import org.oppia.proto.v1.structure.LocalizedExplorationIdDto
import org.oppia.proto.v1.structure.LocalizedRevisionCardIdDto
import org.oppia.proto.v1.structure.ReferencedImageDto
import org.oppia.proto.v1.structure.ReferencedImageListDto
import org.oppia.proto.v1.structure.SetOfLocalizableTextDto
import org.oppia.proto.v1.structure.SingleLocalizableTextDto
import org.oppia.proto.v1.structure.SubtitledTextDto
import org.oppia.proto.v1.structure.SubtopicPageIdDto
import org.oppia.proto.v1.structure.ThumbnailDto
import org.oppia.proto.v1.structure.VoiceoverFileDto
import java.util.Locale

class LocalizationTracker private constructor(
  private val oppiaWebTranslationExtractor: OppiaWebTranslationExtractor,
  private val imageDownloader: ImageDownloader
) {
  // TODO: Translations can come from four places:
  // - SubtitledHtml (directly embedded)
  // - WrittenTranslations (in-structure mapping)
  // - Translations endpoint (out-of-structure mapping, questions & explorations only)
  // - Oppia web, select content
  // Note subtitles might be missing for certain contexts. Content IDs may need to be reconstructed.

  private val containers by lazy { mutableMapOf<ContainerId, Container>() }

  fun initializeContainer(id: ContainerId, defaultLanguage: LanguageType) {
    require(id !in containers) {
      "Container already initialized, ID: $id, attempted language: $defaultLanguage, initialized" +
        " language: ${containers[id]?.defaultLanguage}."
    }
    require(defaultLanguage.isValid()) {
      "Trying to initialize container with ID: $id with invalid default language: $defaultLanguage."
    }
    containers[id] = Container(id, defaultLanguage, imageDownloader)
  }

  fun trackThumbnail(
    id: ContainerId,
    thumbnailFilename: String?,
    thumbnailBackgroundColor: String?,
    thumbnailSizeInBytes: Int?
  ) {
    require(thumbnailFilename != null) {
      "Expected thumbnail filename to be provided for container: $id."
    }
    require(thumbnailBackgroundColor != null) {
      "Expected thumbnail background color to be provided for container: $id."
    }
    require(thumbnailSizeInBytes != null) {
      "Expected thumbnail size to be provided for container: $id."
    }
    val thumbnail = ThumbnailDto.newBuilder().apply {
      this.referencedImage = ReferencedImageDto.newBuilder().apply {
        this.filename = thumbnailFilename
        this.fileSizeBytes = thumbnailSizeInBytes
      }.build()
      this.backgroundColorRgb = checkNotNull(thumbnailBackgroundColor.parseColorRgb()) {
        "Expected string to start with '#' and be a 6-digit hex string. Encountered:" +
          " '$thumbnailBackgroundColor'."
      }
    }.build()
    getExpectedContainer(id).recordDefaultThumbnail(thumbnail)
  }

  fun trackContainerText(id: ContainerId, subtitledText: SubtitledText) {
    getExpectedContainer(id).recordDefaultText(subtitledText)
  }

  fun trackContainerText(id: ContainerId, context: ContentContext, defaultText: String) {
    val container = getExpectedContainer(id)
    container.recordDefaultText(context, defaultText)

    // Also, add Oppia web-tied translations of this text.
    val xlationId = checkNotNull(container.id.webTranslatableActivityId) {
      "Container with ID: $id cannot use Oppia web translations for context: $context. This" +
        " type of container is unsupported."
    }
    val contentId = context.assumedContentId
    val translations = oppiaWebTranslationExtractor.retrieveTranslations(xlationId, contentId)
    val defaultLanguage = container.defaultLanguage
    translations.forEach { (language, text) ->
      // TODO: Figure out which text should be used. It seems that the English translations on web
      //  sometimes don't match the default text within the structures (such as for topic
      //  iX9kYCjnouWN).
      if (language != defaultLanguage) container.recordSingleTranslation(language, contentId, text)
    }
  }

  fun trackContainerText(id: ContainerId, contentId: String, initialTexts: List<String>) {
    getExpectedContainer(id).recordDefaultText(contentId, initialTexts)
  }

  fun convertContainerText(id: ContainerId, subtitledHtml: GaeSubtitledHtml): SubtitledTextDto =
    getExpectedContainer(id).convertDefaultText(subtitledHtml)

  fun convertContainerText(
    id: ContainerId,
    subtitledUnicode: GaeSubtitledUnicode
  ): SubtitledTextDto = getExpectedContainer(id).convertDefaultText(subtitledUnicode)

  fun convertContainerText(id: ContainerId, context: ContentContext): SubtitledTextDto =
    getExpectedContainer(id).convertDefaultText(context)

  fun verifyContentId(id: ContainerId, contentId: String): String =
    contentId.also { getExpectedContainer(id).verifyContentId(it) }

  fun trackTranslations(id: ContainerId, writtenTranslations: GaeWrittenTranslations) {
    val container = getExpectedContainer(id)
    writtenTranslations.translationsMapping.forEach { (contentId, languageTranslations) ->
      languageTranslations.forEach { (languageCode, writtenTranslation) ->
        val language = languageCode.resolveLanguageCode()
        if (language.isValid()) {
          when (val translation = writtenTranslation.translation) {
            is GaeWrittenTranslation.Translation.SingleString ->
              container.recordSingleTranslation(language, contentId, translation.value)
            is GaeWrittenTranslation.Translation.StringList ->
              container.recordMultiTranslation(language, contentId, translation.value)
          }
        }
      }
    }
  }

  fun trackTranslations(id: ContainerId, entityTranslations: GaeEntityTranslation) {
    val container = getExpectedContainer(id)
    val language = entityTranslations.languageCode.resolveLanguageCode()
    if (!language.isValid()) return
    entityTranslations.translations.forEach { (contentId, translatedContent) ->
      when (val translation = translatedContent.contentValue) {
        is GaeTranslatedContent.Translation.SingleString ->
          container.recordSingleTranslation(language, contentId, translation.value)
        is GaeTranslatedContent.Translation.StringList ->
          container.recordMultiTranslation(language, contentId, translation.value)
      }
    }
  }

  fun trackVoiceovers(id: ContainerId, recordedVoiceovers: GaeRecordedVoiceovers) {
    val container = getExpectedContainer(id)
    recordedVoiceovers.voiceoversMapping.forEach { (contentId, languageVoiceovers) ->
      languageVoiceovers.forEach { (languageCode, voiceover) ->
        val language = languageCode.resolveLanguageCode()
        if (language.isValid()) container.recordVoiceover(language, contentId, voiceover.toProto())
      }
    }
  }

  fun isLanguageSupported(id: ContainerId, language: LanguageType): Boolean =
    language in getExpectedContainer(id).getSupportedLanguages()

  suspend fun computeSpecificContentLocalization(
    id: ContainerId,
    language: LanguageType
  ): ContentLocalizationDto = getExpectedContainer(id).computeSpecificContentLocalization(language)

  // TODO: Document that 'defaultLanguage' can redefine the default language of the container based
  //  on available languages.
  suspend fun computeCompleteLocalizationPack(
    id: ContainerId,
    defaultLanguage: LanguageType
  ): ContentLocalizationsDto {
    return getExpectedContainer(id).computeCompleteLocalizationPack(defaultLanguage)
  }

  fun computeAvailableWebTranslations(
    id: ContainerId,
    context: ContentContext
  ): Map<LanguageType, String> {
    val xlationId = checkNotNull(id.webTranslatableActivityId) {
      "Container with ID: $id cannot use Oppia web translations for context: $context. This" +
        " type of container is unsupported."
    }
    val contentId = context.assumedContentId
    return oppiaWebTranslationExtractor.retrieveTranslations(xlationId, contentId)
  }

  private fun getExpectedContainer(id: ContainerId): Container {
    require(id in containers) { "Expected container to be initialized with ID: $id." }
    return containers.getValue(id)
  }

  sealed class ContainerId {
    abstract val webTranslatableActivityId: TranslatableActivityId?
    abstract val gcsImageContainerType: GcsService.ImageContainerType
    abstract val gcsEntityId: String

    data class Exploration(val id: String) : ContainerId() {
      override val webTranslatableActivityId by lazy { TranslatableActivityId.Exploration(id) }
      override val gcsImageContainerType = GcsService.ImageContainerType.EXPLORATION
      override val gcsEntityId = id
    }

    data class Question(val id: String) : ContainerId() {
      override val webTranslatableActivityId = null
      override val gcsImageContainerType = GcsService.ImageContainerType.SKILL
      override val gcsEntityId = id
    }

    data class ConceptCard(val skillId: String) : ContainerId() {
      override val webTranslatableActivityId = null
      override val gcsImageContainerType = GcsService.ImageContainerType.SKILL
      override val gcsEntityId = skillId
    }

    data class RevisionCard(
      val subtopicPageIdDto: SubtopicPageIdDto,
      val webUrlFragment: String
    ) : ContainerId() {
      override val webTranslatableActivityId by lazy {
        TranslatableActivityId.Subtopic(subtopicPageIdDto.topicId, webUrlFragment)
      }
      override val gcsImageContainerType = GcsService.ImageContainerType.TOPIC
      override val gcsEntityId: String = subtopicPageIdDto.topicId
    }

    data class Topic(val id: String) : ContainerId() {
      override val webTranslatableActivityId by lazy { TranslatableActivityId.Topic(id) }
      override val gcsImageContainerType = GcsService.ImageContainerType.TOPIC
      override val gcsEntityId = id
    }

    data class Story(val topicId: String, val storyId: String) : ContainerId() {
      override val webTranslatableActivityId by lazy { TranslatableActivityId.Story(storyId) }
      override val gcsImageContainerType = GcsService.ImageContainerType.STORY
      override val gcsEntityId = storyId
    }

    data class Skill(val skillId: String) : ContainerId() {
      override val webTranslatableActivityId by lazy { TranslatableActivityId.Skill(skillId) }
      override val gcsImageContainerType = GcsService.ImageContainerType.SKILL
      override val gcsEntityId = skillId
    }

    data class Chapter(
      val topicId: String,
      val storyId: String,
      val explorationId: String
    ) : ContainerId() {
      override val webTranslatableActivityId by lazy {
        TranslatableActivityId.Exploration(explorationId)
      }
      override val gcsImageContainerType = GcsService.ImageContainerType.STORY
      override val gcsEntityId = storyId
    }

    companion object {
      fun createFrom(gaeTopic: GaeTopic): ContainerId = Topic(gaeTopic.id)

      fun createFrom(topicId: String, gaeSubtopic: GaeSubtopic): ContainerId {
        val subtopicId = SubtopicPageIdDto.newBuilder().apply {
          this.topicId = topicId
          this.subtopicIndex = gaeSubtopic.id
        }.build()
        return RevisionCard(subtopicId, gaeSubtopic.urlFragment)
      }

      fun createFrom(
        gaeSubtopicPage: GaeSubtopicPage,
        correspondingGaeSubtopic: GaeSubtopic
      ): ContainerId {
        val subtopicId = SubtopicPageIdDto.newBuilder().apply {
          this.topicId = gaeSubtopicPage.topicId
          this.subtopicIndex = correspondingGaeSubtopic.id
        }.build()
        return RevisionCard(subtopicId, correspondingGaeSubtopic.urlFragment)
      }

      fun createFrom(
        revisionCardId: LocalizedRevisionCardIdDto,
        correspondingGaeSubtopic: GaeSubtopic
      ): ContainerId = RevisionCard(revisionCardId.id, correspondingGaeSubtopic.urlFragment)

      fun createFrom(gaeExploration: GaeExploration): ContainerId = Exploration(gaeExploration.id)

      fun createFrom(localizedExplorationId: LocalizedExplorationIdDto): ContainerId =
        Exploration(localizedExplorationId.explorationId)

      fun createFrom(gaeStory: GaeStory): ContainerId =
        Story(gaeStory.correspondingTopicId, gaeStory.id)

      fun createFrom(gaeStory: GaeStory, gaeStoryNode: GaeStoryNode): ContainerId? {
        return gaeStoryNode.explorationId?.let {
          Chapter(gaeStory.correspondingTopicId, gaeStory.id, it)
        }
      }

      fun createFrom(gaeSkill: GaeSkill): ContainerId = ConceptCard(gaeSkill.id)

      fun createFrom(id: LocalizedConceptCardIdDto): ContainerId = ConceptCard(id.skillId)
    }
  }

  enum class ContentContext(val assumedContentId: String) {
    TITLE(assumedContentId = "title"),
    DESCRIPTION(assumedContentId = "description")
  }

  // TODO: Document that content IDs are assumed to be unique across the whole container (which is
  //  guaranteed for all structures now that explorations & questions have a separate translation
  //  structure).
  private class Container(
    val id: ContainerId,
    val defaultLanguage: LanguageType,
    private val imageDownloader: ImageDownloader
  ) {
    private val languages by lazy {
      mutableMapOf(defaultLanguage to TrackedAssets(defaultLanguage))
    }
    private val defaultAssets: TrackedAssets get() = languages.getValue(defaultLanguage)
    private val defaultContentIds: Set<String> get() = defaultAssets.allContentIds
    private val contextsToDownloadFromOppiaWeb = mutableSetOf<ContentContext>()

    fun recordDefaultThumbnail(thumbnail: ThumbnailDto) =
      defaultAssets.recordThumbnail(id, thumbnail)

    fun recordDefaultText(subtitledText: SubtitledText) =
      recordDefaultText(subtitledText.contentId, subtitledText.text)

    fun recordDefaultText(context: ContentContext, text: String) {
      require(context !in contextsToDownloadFromOppiaWeb) {
        "Context is already being tracked to download: $context, for container: $id."
      }
      contextsToDownloadFromOppiaWeb += context
      recordDefaultText(context.assumedContentId, text)
    }

    fun recordDefaultText(contentId: String, texts: List<String>) =
      recordDefaultTexts(contentId, texts)

    fun convertDefaultText(subtitledHtml: GaeSubtitledHtml): SubtitledTextDto =
      convertDefaultText(subtitledHtml.contentId)

    fun convertDefaultText(subtitledUnicode: GaeSubtitledUnicode): SubtitledTextDto =
      convertDefaultText(subtitledUnicode.contentId)

    fun convertDefaultText(context: ContentContext): SubtitledTextDto =
      convertDefaultText(context.assumedContentId)

    fun verifyContentId(contentId: String) = ensureDefaultLanguageHasContent(contentId)

    fun recordSingleTranslation(language: LanguageType, contentId: String, text: String) {
      ensureDefaultLanguageHasContent(contentId)
      retrieveAssetsForLanguage(language).recordSingleTranslation(id, contentId, text)
    }

    fun recordMultiTranslation(language: LanguageType, contentId: String, texts: List<String>) {
      ensureDefaultLanguageHasContent(contentId)
      retrieveAssetsForLanguage(language).recordMultiTranslation(id, contentId, texts)
    }

    fun recordVoiceover(language: LanguageType, contentId: String, voiceover: VoiceoverFileDto) {
      ensureDefaultLanguageHasContent(contentId)
      retrieveAssetsForLanguage(language).recordVoiceover(id, contentId, voiceover)
    }

    fun getSupportedLanguages(): Set<LanguageType> = languages.keys

    suspend fun computeSpecificContentLocalization(
      language: LanguageType
    ): ContentLocalizationDto {
      require(language in languages) {
        "Expected language $language to be supported, but supported languages are:" +
          " ${getSupportedLanguages()}."
      }
      return languages.getValue(language).convertToContentLocalization(
        id.gcsImageContainerType, id.gcsEntityId, imageDownloader
      )
    }

    suspend fun computeCompleteLocalizationPack(
      defaultLanguage: LanguageType
    ): ContentLocalizationsDto {
      return ContentLocalizationsDto.newBuilder().apply {
        this.defaultMapping = computeSpecificContentLocalization(defaultLanguage)
        this.addAllLocalizations(languages.keys.map { computeSpecificContentLocalization(it) })
      }.build()
    }

    private fun recordDefaultText(contentId: String, translatedText: String) {
      defaultAssets.recordSingleTranslation(id, contentId, translatedText)
    }

    private fun recordDefaultTexts(contentId: String, translatedTexts: List<String>) {
      defaultAssets.recordMultiTranslation(id, contentId, translatedTexts)
    }

    private fun convertDefaultText(contentId: String): SubtitledTextDto {
      // This failure indicates a likely code issue within the pipeline since it means an
      // inconsistency between containers that are supposed to be pre-tracked and containers
      // actively being converted to protos.
      ensureDefaultLanguageHasContent(contentId)
      return SubtitledTextDto.newBuilder().apply {
        this.contentId = contentId
      }.build()
    }

    private fun retrieveAssetsForLanguage(language: LanguageType) =
      languages.getOrPut(language) { TrackedAssets(language) }

    private fun ensureDefaultLanguageHasContent(contentId: String) {
      check(contentId in defaultContentIds) {
        "Attempting to add an asset for a content ID that hasn't been defaulted in container:" +
          " $id, content ID: $contentId."
      }
    }
  }

  private data class TrackedAssets(
    val language: LanguageType,
    val textTranslations: MutableMap<String, LocalizableTextDto> = mutableMapOf(),
    val voiceovers: MutableMap<String, VoiceoverFileDto> = mutableMapOf()
  ) {
    var thumbnail: ThumbnailDto? = null

    val allContentIds: Set<String> get() = textTranslations.keys + voiceovers.keys

    fun recordThumbnail(id: ContainerId, thumbnail: ThumbnailDto) {
      require(this.thumbnail == null) {
        "Attempting to record a second thumbnail for container: $id. New thumbnail: $thumbnail," +
          " current thumbnail: ${this.thumbnail}."
      }
      this.thumbnail = thumbnail
    }

    fun recordSingleTranslation(id: ContainerId, contentId: String, translatedText: String) {
      val localization = LocalizableTextDto.newBuilder().apply {
        singleLocalizableText = SingleLocalizableTextDto.newBuilder().apply {
          this.text = translatedText
        }.build()
      }.build()
      recordTranslation(id, contentId, localization)
    }

    fun recordMultiTranslation(id: ContainerId, contentId: String, translatedTexts: List<String>) {
      val localization = LocalizableTextDto.newBuilder().apply {
        setOfLocalizableText = SetOfLocalizableTextDto.newBuilder().apply {
          this.addAllText(translatedTexts)
        }.build()
      }.build()
      recordTranslation(id, contentId, localization)
    }

    fun recordVoiceover(id: ContainerId, contentId: String, voiceover: VoiceoverFileDto) {
      require(contentId !in voiceovers) {
        "Voiceover already recorded for content ID: $contentId, for language: $language, in" +
          " container: $id."
      }
      voiceovers[contentId] = voiceover
    }

    suspend fun convertToContentLocalization(
      imageContainerType: GcsService.ImageContainerType,
      entityId: String,
      imageDownloader: ImageDownloader
    ): ContentLocalizationDto {
      val htmlTexts = textTranslations.values.flatMap { localizableText ->
        when (localizableText.dataFormatCase) {
          LocalizableTextDto.DataFormatCase.SINGLE_LOCALIZABLE_TEXT ->
            listOf(localizableText.singleLocalizableText.text)
          LocalizableTextDto.DataFormatCase.SET_OF_LOCALIZABLE_TEXT ->
            localizableText.setOfLocalizableText.textList
          LocalizableTextDto.DataFormatCase.DATAFORMAT_NOT_SET, null ->
            error("Unsupported localizable text: $localizableText.")
        }
      }.distinct()
      val referencedImageFilenames =
        htmlTexts.flatMapTo(mutableSetOf(), ::collectAllImageSourcesFromHtml)

      // Batch all of the image requests together so that they can run in parallel.
      val imageSizes = referencedImageFilenames.map { filename ->
        imageDownloader.retrieveImageLengthAsync(
          imageContainerType, GcsService.ImageType.HTML_IMAGE, entityId, filename
        ) { filename to it }
      }.awaitAll().toMap()
      val referencedImages = referencedImageFilenames.map { filename ->
        ReferencedImageDto.newBuilder().apply {
          this.filename = filename
          this.fileSizeBytes = imageSizes.getValue(filename)
        }.build()
      }

      return ContentLocalizationDto.newBuilder().apply {
        this.protosVersion = ProtoVersionProvider.createLatestLanguageProtosVersion()
        this.language = this@TrackedAssets.language
        this.putAllLocalizableTextContentMapping(this@TrackedAssets.textTranslations)
        this.putAllVoiceoverContentMapping(this@TrackedAssets.voiceovers)
        this@TrackedAssets.thumbnail?.let { this.thumbnail = it }
        this.localizedImageList = ReferencedImageListDto.newBuilder().apply {
          this.protoVersion = ProtoVersionProvider.createLatestImageProtoVersion()
          this.addAllReferencedImages(referencedImages)
        }.build()
      }.build()
    }

    private fun recordTranslation(
      id: ContainerId,
      contentId: String,
      localization: LocalizableTextDto
    ) {
      require(contentId !in textTranslations) {
        "Translation already recorded for content ID: $contentId, for language: $language, in" +
          " container: $id."
      }
      textTranslations[contentId] = localization
    }
  }

  @JsonClass(generateAdapter = true)
  data class MathContentValue(
    @Json(name = "raw_latex") val rawLatex: String,
    @Json(name = "svg_filename") val svgFilename: String
  ) {
    companion object {
      private val moshi by lazy { Moshi.Builder().build() }
      private val adapter by lazy { moshi.adapter(MathContentValue::class.java) }

      internal fun parseFromHtmlValue(htmlValue: String): MathContentValue {
        return adapter.fromJson(htmlValue)
          ?: error("Failed to parse content value from: $htmlValue")
      }
    }
  }

  companion object {
    private val HEX_CHARACTERS = "abcdef1234567890".toList()
    private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
    private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
    private const val CUSTOM_MATH_TAG = "oppia-noninteractive-math"
    private const val CUSTOM_MATH_SVG_PATH_ATTRIBUTE = "math_content-with-value"
    private val customImageTagRegex by lazy {
      Regex("<\\s*$CUSTOM_IMG_TAG.+?$CUSTOM_IMG_FILE_PATH_ATTRIBUTE\\s*=\\s*\"(.+?)\"")
    }
    private val customMathTagRegex by lazy {
      Regex("<\\s*$CUSTOM_MATH_TAG.+?$CUSTOM_MATH_SVG_PATH_ATTRIBUTE\\s*=\\s*\"(.+?)\"")
    }
    val VALID_LANGUAGE_TYPES = LanguageType.values().filter { it.isValid() }

    suspend fun createTracker(imageDownloader: ImageDownloader): LocalizationTracker =
      LocalizationTracker(OppiaWebTranslationExtractor.createExtractor(), imageDownloader)

    fun String.resolveLanguageCode(): LanguageType {
      return when (toLowerCase(Locale.US)) {
        "en", "en_us", "en-us" -> LanguageType.ENGLISH
        "ar" -> LanguageType.ARABIC
        "hi" -> LanguageType.HINDI
        "hi-en" -> LanguageType.HINGLISH
        "pt", "pt-br" -> LanguageType.BRAZILIAN_PORTUGUESE
        "sw" -> LanguageType.SWAHILI
        "pcm" -> LanguageType.NIGERIAN_PIDGIN
        else -> LanguageType.UNRECOGNIZED
      }
    }

    fun String.parseColorRgb(): Int? {
      return if (startsWith("#") && length == 7 && substring(1).isHexString()) {
        substring(1).toIntOrNull(radix = 16)
      } else null
    }

    private fun String.isHexString(): Boolean = all { it.isHex() }

    private fun Char.isHex(): Boolean = toLowerCase() in HEX_CHARACTERS

    fun LanguageType.isValid(): Boolean =
      this != LanguageType.LANGUAGE_CODE_UNSPECIFIED && this != LanguageType.UNRECOGNIZED

    private fun GaeVoiceover.toProto() = VoiceoverFileDto.newBuilder().apply {
      this.filename = this@toProto.filename
      this.fileSizeBytes = this@toProto.fileSizeBytes
      this.durationSecs = this@toProto.durationSecs
    }.build()

    private fun collectAllImageSourcesFromHtml(html: String) =
      collectImageSourcesFromHtml(html) + collectMathSourcesFromHtml(html)

    private fun collectImageSourcesFromHtml(html: String): Set<String> {
      return customImageTagRegex.findAll(html)
        .map { it.destructured }
        .map { (match) -> match }
        .map { it.replace("&amp;quot;", "") } // Clean up the HTML.
        .filter { it.isNotEmpty() } // Ignore incorrect missing images.
        .toSet()
    }

    private fun collectMathSourcesFromHtml(html: String): Set<String> {
      return customMathTagRegex.findAll(html)
        .map { it.destructured }
        .map { (match) -> match }
        .map { it.replace("&amp;quot;", "\"") }
        .map { MathContentValue.parseFromHtmlValue(it) }
        .map { it.svgFilename }
        .filter { it.isNotEmpty() } // Ignore incorrect missing images.
        .toSet()
    }
  }
}
