package org.oppia.android.scripts.assets

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Types
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.oppia.android.app.model.QuestionList
import org.oppia.android.scripts.assets.DtoProtoToLegacyProtoConverter.convertToQuestion
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityConstraints
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure.StateSchemaVersionTooNew
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityResult
import org.oppia.android.scripts.gae.compat.SubtitledHtmlCollector
import org.oppia.android.scripts.gae.gcs.GcsService
import org.oppia.android.scripts.gae.json.GaeQuestion
import org.oppia.android.scripts.gae.json.MoshiFactory
import org.oppia.android.scripts.gae.proto.ImageDownloader
import org.oppia.android.scripts.gae.proto.JsonToProtoConverter
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.OppiaWebTranslationExtractor
import org.oppia.android.scripts.proto.DownloadConfig
import org.oppia.proto.v1.structure.ContentLocalizationDto
import org.oppia.proto.v1.structure.DownloadableTopicSummaryDto
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.LocalizableTextDto
import org.oppia.proto.v1.structure.LocalizedQuestionIdDto
import org.oppia.proto.v1.structure.QuestionDto
import org.oppia.proto.v1.structure.QuestionLanguagePackDto
import org.oppia.proto.v1.structure.SingleLocalizableTextDto
import org.oppia.proto.v1.structure.SubtopicSummaryDto
import java.io.File
import java.net.InetSocketAddress
import kotlin.test.assertFailsWith

/** Tests the question JSON-to-DTO-to-app conversion and the offline practice entry-point checks. */
@Suppress("FunctionName")
class PracticeQuestionAssetsTest {
  private lateinit var sourceQuestions: List<GaeQuestion>
  private lateinit var questions: List<QuestionDto>
  private lateinit var tracker: LocalizationTracker
  private lateinit var imageServer: HttpServer
  private lateinit var solutionExclusivity: Map<String, Boolean>

  @Before
  fun setUp() = runBlocking {
    imageServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    imageServer.createContext("/") { exchange ->
      exchange.responseHeaders.add("Content-Length", "1")
      exchange.sendResponseHeaders(200, -1)
      exchange.close()
    }
    imageServer.start()
    val imageBaseUrl = "http://127.0.0.1:${imageServer.address.port}"
    val adapter = MoshiFactory.createMoshi().adapter<List<GaeQuestion>>(
      Types.newParameterizedType(List::class.java, GaeQuestion::class.java)
    )
    sourceQuestions = checkNotNull(
      adapter.fromJson(File("scripts/assets/practice_question_test.json").readText())
    )
    tracker = LocalizationTracker.createTracker(
      ImageDownloader(GcsService(imageBaseUrl, "test"), Dispatchers.Unconfined),
      DownloadConfig.getDefaultInstance(),
      OppiaWebTranslationExtractor.createFromTranslations(emptyMap())
    )
    solutionExclusivity = sourceQuestions.mapNotNull { question ->
      question.questionStateData.interaction?.solution?.let {
        question.id to it.answerIsExclusive
      }
    }.toMap()
    val converter = JsonToProtoConverter(tracker, emptyMap())
    converter.trackQuestionTranslations(sourceQuestions)
    questions = sourceQuestions.map { converter.convertToQuestion(it, LanguageType.ENGLISH) }
  }

  @After
  fun tearDown() {
    imageServer.stop(0)
  }

  @Test
  fun testCreateQuestionList_publicAndDeveloperFixtures_roundTripPreservesPlayableContent() {
    val list = PracticeQuestionAssets.createQuestionList(
      questions, emptyList(), emptyMap(), solutionExclusivity
    )
    val parsed = QuestionList.parseFrom(list.toByteArray())

    assertThat(parsed.questionsList.map { it.questionId })
      .containsExactlyElementsIn(sourceQuestions.map { it.id }.sorted()).inOrder()
    sourceQuestions.forEach { source ->
      val actual = parsed.questionsList.single { it.questionId == source.id }
      val interaction = checkNotNull(source.questionStateData.interaction)
      assertThat(actual.version).isEqualTo(source.version)
      assertThat(actual.languageCode).isEqualTo(source.languageCode)
      assertThat(actual.linkedSkillIdsList).containsExactlyElementsIn(source.linkedSkillIds)
      assertThat(actual.questionState.name).isEqualTo("question")
      assertThat(actual.questionState.content.html).isEqualTo(source.questionStateData.content.text)
      assertThat(actual.questionState.interaction.id).isEqualTo(interaction.id)
      assertThat(
        actual.questionState.interaction.answerGroupsList.map {
          it.outcome.labelledAsCorrect
        }
      ).containsExactlyElementsIn(
        interaction.answerGroups.map {
          it.outcome.labelledAsCorrect
        }
      ).inOrder()
      assertThat(actual.questionState.interaction.hintList.map { it.hintContent.html })
        .containsExactlyElementsIn(interaction.hints.map { it.hintContent.text }).inOrder()
      assertThat(
        actual.questionState.interaction.answerGroupsList.map {
          it.taggedSkillMisconception.skillId.takeIf { id -> id.isNotEmpty() }?.let { id ->
            "$id-${it.taggedSkillMisconception.misconceptionId}"
          }
        }
      ).containsExactlyElementsIn(
        interaction.answerGroups.map {
          it.taggedSkillMisconceptionId?.takeIf { id -> id.isNotEmpty() }
        }
      ).inOrder()
      interaction.solution?.let { solution ->
        assertThat(actual.questionState.interaction.solution.answerIsExclusive)
          .isEqualTo(solution.answerIsExclusive)
        assertThat(actual.questionState.interaction.solution.explanation.html)
          .isEqualTo(solution.explanation.text)
        assertThat(actual.questionState.interaction.solution.correctAnswer.objectTypeCase.name)
          .isNotEqualTo("OBJECTTYPE_NOT_SET")
      }
    }
    assertThat(parsed.questionsList.map { it.questionState.interaction.id }.toSet())
      .containsAtLeast("TextInput", "NumericInput", "MultipleChoiceInput")
  }

  @Test
  fun testCreateQuestionList_missingSolutionExclusivity_throws() {
    val error = assertFailsWith<IllegalStateException> {
      PracticeQuestionAssets.createQuestionList(questions, emptyList(), emptyMap(), emptyMap())
    }

    assertThat(error).hasMessageThat().contains("Missing solution exclusivity")
  }

  @Test
  fun testCreateQuestionList_duplicateQuestionIds_throws() {
    val error = assertFailsWith<IllegalStateException> {
      PracticeQuestionAssets.createQuestionList(
        listOf(questions[0], questions[0]), emptyList(), emptyMap(), solutionExclusivity
      )
    }

    assertThat(error).hasMessageThat().contains("duplicate IDs")
  }

  @Test
  fun testConvertQuestion_additionalLanguage_preservesTranslatedContent() {
    val question = questions[0]
    val contentId = question.questionState.content.contentId
    val pack = QuestionLanguagePackDto.newBuilder().apply {
      id = LocalizedQuestionIdDto.newBuilder()
        .setQuestionId(question.id).setLanguage(LanguageType.ARABIC).build()
      contentVersion = question.contentVersion
      localization = ContentLocalizationDto.newBuilder().apply {
        language = LanguageType.ARABIC
        putLocalizableTextContentMapping(contentId, createText("<p>سؤال</p>"))
      }.build()
    }.build()

    val actual = question.convertToQuestion(emptyMap(), listOf(pack))

    assertThat(
      actual.questionState.writtenTranslationsMap.getValue(contentId)
        .translationMappingMap.getValue("ar").html
    ).isEqualTo("<p>سؤال</p>")
  }

  @Test
  fun testConvertQuestion_mismatchedPackVersion_throws() {
    val question = questions[0]
    val pack = QuestionLanguagePackDto.newBuilder().apply {
      id = LocalizedQuestionIdDto.newBuilder().setQuestionId(question.id).build()
      contentVersion = question.contentVersion + 1
    }.build()

    val error = assertFailsWith<IllegalStateException> {
      question.convertToQuestion(emptyMap(), listOf(pack))
    }

    assertThat(error).hasMessageThat().contains("must match question")
  }

  @Test
  fun testCreateQuestionList_orphanLanguagePack_throws() {
    val pack = QuestionLanguagePackDto.newBuilder().setId(
      LocalizedQuestionIdDto.newBuilder().setQuestionId("missing").build()
    ).build()

    val error = assertFailsWith<IllegalStateException> {
      PracticeQuestionAssets.createQuestionList(
        questions, listOf(pack), emptyMap(), solutionExclusivity
      )
    }

    assertThat(error).hasMessageThat().contains("no matching downloaded question")
  }

  @Test
  fun testConvertQuestion_renamedImage_rewritesDefaultContent() {
    val original = questions[0]
    val contentId = original.questionState.content.contentId
    val question = original.toBuilder().setDefaultLocalization(
      original.defaultLocalization.toBuilder().putLocalizableTextContentMapping(
        contentId,
        createText(
          "<oppia-noninteractive-image filepath-with-value=\"&amp;quot;old.svg&amp;quot;\">" +
            "</oppia-noninteractive-image>"
        )
      ).build()
    ).build()

    val actual = question.convertToQuestion(mapOf("old.svg" to "repaired.png"), emptyList())

    assertThat(actual.questionState.content.html).contains("repaired.png")
    assertThat(actual.questionState.content.html).doesNotContain("old.svg")
  }

  @Test
  fun testHasPracticeQuestions_topicToggleOff_returnsFalseDespiteQuestions() {
    assertThat(
      PracticeQuestionAssets.hasPracticeQuestions(
        createTopic(listOf(listOf("skill_0"))), false, createQuestions(listOf("skill_0"))
      )
    ).isFalse()
  }

  @Test
  fun testHasPracticeQuestions_topicToggleOnWithEverySubtopicCovered_returnsTrue() {
    assertThat(
      PracticeQuestionAssets.hasPracticeQuestions(
        createTopic(listOf(listOf("skill_0", "skill_1"), listOf("skill_2"))), true,
        createQuestions(listOf("skill_1", "skill_2"))
      )
    ).isTrue()
  }

  @Test
  fun testHasPracticeQuestions_oneSubtopicMissingQuestions_returnsFalse() {
    assertThat(
      PracticeQuestionAssets.hasPracticeQuestions(
        createTopic(listOf(listOf("skill_0"), listOf("skill_1"))), true,
        createQuestions(listOf("skill_0"))
      )
    ).isFalse()
  }

  @Test
  fun testHasPracticeQuestions_noQuestions_returnsFalse() {
    assertThat(
      PracticeQuestionAssets.hasPracticeQuestions(
        createTopic(listOf(listOf("skill_0"))), true, QuestionList.getDefaultInstance()
      )
    ).isFalse()
  }

  @Test
  fun testHasPracticeQuestions_noSubtopics_returnsFalse() {
    assertThat(
      PracticeQuestionAssets.hasPracticeQuestions(
        createTopic(emptyList()), true, createQuestions(listOf("skill_0"))
      )
    ).isFalse()
  }

  @Test
  fun testQuestionCompatibility_futureStateSchema_isRejected() {
    val source = sourceQuestions[0]
    val checker = StructureCompatibilityChecker(
      CompatibilityConstraints(
        supportedInteractionIds = setOf("TextInput"),
        supportedDefaultLanguages = setOf(LanguageType.ENGLISH),
        requiredTranslationLanguages = emptySet(),
        supportedImageFormats = setOf("png", "svg"),
        supportedAudioFormats = emptySet(),
        supportedHtmlTags = setOf("p", "strong", "b", "em", "span", "br"),
        supportedStateSchemaVersion = source.questionStateDataSchemaVersion,
        topicDependencies = emptyMap(),
        forcedVersions = null
      ),
      tracker, SubtitledHtmlCollector(tracker)
    )

    val result = checker.isQuestionItselfCompatible(
      source.copy(questionStateDataSchemaVersion = source.questionStateDataSchemaVersion + 1)
    )

    assertThat(result).isInstanceOf(CompatibilityResult.Incompatible::class.java)
    assertThat(
      (result as CompatibilityResult.Incompatible).failures.any {
        it is StateSchemaVersionTooNew
      }
    ).isTrue()
  }

  @Test
  fun testQuestionImageContainer_usesQuestionAssetsDirectory() {
    val container = LocalizationTracker.ContainerId.Question("question_0")
    val url = GcsService("https://storage.googleapis.com", "oppiaserver-resources").computeImageUrl(
      container.gcsImageContainerType, GcsService.ImageType.HTML_IMAGE,
      container.gcsEntityId, "image.svg"
    )

    assertThat(url).isEqualTo(
      "https://storage.googleapis.com/oppiaserver-resources/question/question_0" +
        "/assets/image/image.svg"
    )
  }

  private fun createText(text: String): LocalizableTextDto = LocalizableTextDto.newBuilder()
    .setSingleLocalizableText(SingleLocalizableTextDto.newBuilder().setText(text).build()).build()

  private fun createTopic(skillIds: List<List<String>>): DownloadableTopicSummaryDto =
    DownloadableTopicSummaryDto.newBuilder().apply {
      id = "topic_0"
      addAllSubtopicSummaries(
        skillIds.mapIndexed { index, ids ->
          SubtopicSummaryDto.newBuilder().setIndex(index).addAllReferencedSkillIds(ids).build()
        }
      )
    }.build()

  private fun createQuestions(skills: List<String>): QuestionList =
    QuestionList.newBuilder().apply {
      addQuestions(
        questions[0].toBuilder().clearLinkedSkillIds().addAllLinkedSkillIds(skills).build()
          .convertToQuestion(emptyMap(), emptyList())
      )
    }.build()
}
